import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { createRequire } from 'node:module';
import test from 'node:test';
import { pathToFileURL } from 'node:url';
import vm from 'node:vm';

const workerSource = (await readFile(new URL('./worker.js', import.meta.url), 'utf8'))
    .replace("import sqlite3InitModule from '@sqlite.org/sqlite-wasm';", '');

function createWorker(sqlite3Promise, storage) {
    const messages = [];
    let initializationConfig;
    const context = vm.createContext({
        navigator: { storage },
        sqlite3InitModule: () => {
            initializationConfig = structuredClone(context.sqlite3ApiConfig);
            return sqlite3Promise;
        },
        postMessage: message => messages.push(structuredClone(message))
    });
    const ready = vm.runInContext(workerSource, context);
    let nextRequestId = 0;
    return {
        messages,
        ready: () => ready,
        get initializationConfig() { return initializationConfig; },
        request(data) {
            const id = nextRequestId++;
            context.onmessage({ data: { id, data } });
            return messages.find(message => message.id === id);
        }
    };
}

test('uses OPFS and reports persistent storage when installed', async () => {
    // Given the browser grants access to OPFS and SQLite can install its VFS.
    const openedFiles = [];
    const worker = createWorker(Promise.resolve({ oo1: {
        OpfsDb: class { constructor(fileName) { openedFiles.push(fileName); } },
        DB: class { constructor() { throw new Error('Unexpected volatile fallback'); } }
    } }), { getDirectory: async () => ({}) });
    await worker.ready();

    // When the driver opens the wallet database.
    const response = worker.request({ cmd: 'open', fileName: 'wallet.db' });

    // Then SQLite initializes normally and opens the named persistent database.
    assert.equal(worker.initializationConfig, undefined);
    assert.deepEqual(openedFiles, ['wallet.db']);
    assert.deepEqual(response, { id: 0, data: { databaseId: 0, storageMode: 'persistent' } });
});

test('disables both OPFS VFSes before SQLite initializes when the browser denies access', async () => {
    // Given a browser that exposes the storage API but denies access to OPFS.
    const worker = createWorker(Promise.resolve({ oo1: {} }), {
        getDirectory: async () => { throw new DOMException('OPFS access denied', 'SecurityError'); }
    });

    // When the worker initializes SQLite.
    await worker.ready();

    // Then SQLite skips both OPFS installers.
    assert.deepEqual(worker.initializationConfig, { disable: { vfs: { opfs: true, 'opfs-wl': true } } });
});

test('leaves OPFS initialization to SQLite when the probe returns an unexpected error', async () => {
    // Given a directory probe that fails without a security rejection.
    const worker = createWorker(Promise.resolve({ oo1: {} }), {
        getDirectory: async () => { throw new DOMException('Storage operation failed', 'UnknownError'); }
    });

    // When the worker initializes SQLite.
    await worker.ready();

    // Then both VFS installers remain under SQLite's control.
    assert.equal(worker.initializationConfig, undefined);
});

test('uses named volatile files when OPFS is unavailable', async () => {
    // Given SQLite initialized without the OPFS class, as after a private-mode rejection.
    const openedFiles = [];
    const worker = createWorker(Promise.resolve({ oo1: {
        DB: class { constructor(fileName) { openedFiles.push(fileName); } }
    } }));
    await worker.ready();

    // When Room opens reader and writer connections to the same database.
    const writer = worker.request({ cmd: 'open', fileName: 'wallet.db' });
    const reader = worker.request({ cmd: 'open', fileName: 'wallet.db' });

    // Then both use the same named volatile file and report temporary storage.
    assert.deepEqual(openedFiles, ['wallet.db', 'wallet.db']);
    assert.deepEqual(writer.data, { databaseId: 0, storageMode: 'temporary' });
    assert.deepEqual(reader.data, { databaseId: 1, storageMode: 'temporary' });
});

test('propagates OPFS open errors without switching databases', async () => {
    // Given OPFS installed successfully but opening a persistent file fails.
    let volatileOpens = 0;
    const worker = createWorker(Promise.resolve({ oo1: {
        OpfsDb: class { constructor() { throw new Error('database is locked'); } },
        DB: class { constructor() { volatileOpens++; } }
    } }));
    await worker.ready();

    // When the driver opens the database.
    const response = worker.request({ cmd: 'open', fileName: 'wallet.db' });

    // Then the ordinary database failure is preserved, with no temporary replacement.
    assert.deepEqual(response, { id: 0, error: 'database is locked' });
    assert.equal(volatileOpens, 0);
});

test('drains queued opens after SQLite initialization selects temporary storage', async () => {
    // Given worker requests arriving before SQLite has finished initializing.
    let initialize;
    const initialized = new Promise(resolve => { initialize = resolve; });
    const openedFiles = [];
    const worker = createWorker(initialized);
    worker.request({ cmd: 'open', fileName: 'wallet.db' });
    worker.request({ cmd: 'open', fileName: 'wallet.db' });
    assert.deepEqual(worker.messages, []);

    // When initialization completes without OPFS.
    initialize({ oo1: {
        DB: class { constructor(fileName) { openedFiles.push(fileName); } }
    } });
    await worker.ready();

    // Then requests complete in order with the same fallback and filename.
    assert.deepEqual(openedFiles, ['wallet.db', 'wallet.db']);
    assert.deepEqual(worker.messages, [
        { id: 0, data: { databaseId: 0, storageMode: 'temporary' } },
        { id: 1, data: { databaseId: 1, storageMode: 'temporary' } }
    ]);
});

let sqliteModulePath;
try {
    // Gradle installs this package here; direct npm installs resolve locally instead.
    const localRequire = createRequire(import.meta.url);
    const gradleRequire = createRequire(new URL('../../build/wasm/package.json', import.meta.url));
    try {
        sqliteModulePath = localRequire.resolve('@sqlite.org/sqlite-wasm');
    } catch {
        sqliteModulePath = gradleRequire.resolve('@sqlite.org/sqlite-wasm');
    }
} catch (error) {
    if (error.code !== 'MODULE_NOT_FOUND') throw error;
}

test('real SQLite shares temporary data between connections and after all handles close', {
    skip: sqliteModulePath ? false : 'Install SQLite dependencies with npm or the Gradle web build first'
}, async () => {
    // Given real SQLite with its default MEMFS VFS and two Room-style connections.
    const { default: initializeSqlite } = await import(pathToFileURL(sqliteModulePath));
    const sqlite3 = await initializeSqlite();
    assert.equal(sqlite3.oo1.OpfsDb, undefined);
    const worker = createWorker(Promise.resolve(sqlite3));
    await worker.ready();
    function open() {
        const response = worker.request({ cmd: 'open', fileName: 'temporary-wallet.db' });
        assert.equal(response.error, undefined);
        assert.equal(response.data.storageMode, 'temporary');
        return response.data.databaseId;
    }
    function execute(databaseId, sql, bindings = []) {
        const prepared = worker.request({ cmd: 'prepare', databaseId, sql });
        assert.equal(prepared.error, undefined);
        const statementId = prepared.data.statementId;
        try {
            const response = worker.request({ cmd: 'step', statementId, bindings });
            assert.equal(response.error, undefined);
            return response.data.rows;
        } finally {
            worker.request({ cmd: 'close', statementId });
        }
    }
    const writer = open();
    const reader = open();

    // When the writer commits data and every database handle is subsequently closed.
    execute(writer, 'CREATE TABLE wallet (name TEXT NOT NULL)');
    execute(writer, 'INSERT INTO wallet (name) VALUES (?)', ['session wallet']);
    const readerRows = execute(reader, 'SELECT name FROM wallet');
    worker.request({ cmd: 'close', databaseId: writer });
    worker.request({ cmd: 'close', databaseId: reader });
    const reopened = open();
    const reopenedRows = execute(reopened, 'SELECT name FROM wallet');
    worker.request({ cmd: 'close', databaseId: reopened });

    // Then both another connection and a later reopen see the same session data.
    assert.deepEqual(readerRows, [['session wallet']]);
    assert.deepEqual(reopenedRows, [['session wallet']]);
});

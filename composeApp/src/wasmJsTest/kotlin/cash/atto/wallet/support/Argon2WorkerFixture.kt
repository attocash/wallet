@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.support

internal fun observeArgon2Workers(fault: String = ""): Unit =
    js(
        """
    {
        const OriginalWorker = window.Worker;
        const originalTimeout = window.setTimeout;
        window.__argon2Workers = {active:0, maximum:0};
        window.__restoreArgon2Workers = () => { window.Worker = OriginalWorker; window.setTimeout = originalTimeout; };
        if (fault === 'timeout') window.setTimeout = (handler, delay, ...args) => originalTimeout(handler, delay === 30000 ? 5 : delay, ...args);
        window.Worker = function(url, options) {
            if (options?.name !== 'wallet-argon2') return new OriginalWorker(url, options);
            if (fault === 'constructor') throw new DOMException('Synthetic worker denial', 'SecurityError');
            const stats = window.__argon2Workers;
            stats.active++;
            stats.maximum = Math.max(stats.maximum, stats.active);
            const worker = fault ? new EventTarget() : new OriginalWorker(url, options);
            const terminate = fault ? () => {} : worker.terminate.bind(worker);
            let active = true;
            worker.terminate = () => { if (active) { active = false; stats.active--; } terminate(); };
            if (fault) worker.postMessage = () => {
                if (fault === 'error') originalTimeout(() => worker.onerror?.(new Event('error', {cancelable:true})), 0);
                if (fault === 'response') originalTimeout(() => worker.onmessage?.(new MessageEvent('message', {data:{error:true}})), 0);
            };
            return worker;
        };
    }
    """,
    )

internal fun activeArgon2Workers(): Int = js("window.__argon2Workers.active")

internal fun maximumArgon2Workers(): Int = js("window.__argon2Workers.maximum")

internal fun restoreArgon2Workers(): Unit = js("window.__restoreArgon2Workers()")

#!/usr/bin/env node
// Manual oracle refresh only. This script is not part of a build or application runtime.
import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import fs from 'node:fs';
import { createRequire } from 'node:module';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const referenceModules = process.argv[2];
if (!referenceModules || process.argv.length !== 3) {
  throw new Error('Usage: node scripts/generate-password-strength-reference.mjs /absolute/path/to/reference/node_modules');
}
assert(path.isAbsolute(referenceModules), 'Pass an explicit absolute path to existing reference packages; nothing is installed.');
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const require = createRequire(import.meta.url);
const expectedVersions = { core: '4.2.0', 'language-common': '4.1.3', 'language-en': '4.1.1' };
for (const [name, version] of Object.entries(expectedVersions)) {
  const pkg = JSON.parse(fs.readFileSync(path.join(referenceModules, '@zxcvbn-ts', name, 'package.json'), 'utf8'));
  assert.equal(pkg.version, version, `Unexpected ${name} version`);
}

// The upstream module captures getFullYear() when it is loaded.
const ActualDate = globalThis.Date;
globalThis.Date = class ReferenceDate extends ActualDate {
  getFullYear() { return 2026; }
};
const { ZxcvbnFactory } = require(path.join(referenceModules, '@zxcvbn-ts/core'));
globalThis.Date = ActualDate;
const common = require(path.join(referenceModules, '@zxcvbn-ts/language-common'));
const english = require(path.join(referenceModules, '@zxcvbn-ts/language-en'));
const dictionary = { ...common.dictionary, ...english.dictionary };
// BundledPasswordDataTest independently verifies the complete checked-in Kotlin data.
const estimator = new ZxcvbnFactory({ dictionary, graphs: common.adjacencyGraphs, maxLength: 256 });
const context = ['atto', 'wallet', 'attocash', 'attowallet', 'atto wallet'];
const passwords = new Map();
const add = (group, ...values) => {
  for (const password of values) if (!passwords.has(password)) passwords.set(password, group);
};

add('basic/context', '', 'a', 'A', '0', '!', ' ', '\t', '\n', 'password', 'Password', 'PASSWORD', 'pAsSwOrD',
  'password1', 'Password1!', 'password123', 'passw0rd', 'p@ssword', 'p@ssw0rd', 'P@$$w0rd!', '123456', '1234567890',
  'letmein', 'correct horse battery staple', 'correcthorsebatterystaple', 'Tr0ub4dor&3', 'hunter2',
  ...context, ...context.map(x => x.toUpperCase()), ...context.map(x => `${x}123!`),
  'atto-wallet', 'atto_wallet', 'atto.cash', 'AttoWallet2026!', 'walletatto', 'attocashwallet', 'ellaw', 'tellaw',
  'Atto Cash Wallet', 'attowalletattowallet');

for (const [name, words] of Object.entries(dictionary)) {
  const indices = [0, 1, 9, 99, Math.floor(words.length / 2), words.length - 1];
  for (const index of new Set(indices.filter(x => x >= 0 && x < words.length))) {
    const word = String(words[index]);
    add(`dictionary/${name}`, word, word[0]?.toUpperCase() + word.slice(1), [...word].reverse().join(''));
  }
}

for (const word of ['password', 'administrator', 'iloveyou', 'dragon', 'monkey', 'welcome', 'football', 'sunshine',
  'princess', 'letmein', 'computer', 'baseball', 'secret', 'hello', 'summer', 'michael', 'jennifer', 'alexander']) {
  const leet = word.replaceAll('a', '@').replaceAll('e', '3').replaceAll('i', '1').replaceAll('o', '0').replaceAll('s', '$');
  add('leet/case/reverse', word.toUpperCase(), leet, `${leet}!`, `${leet}2026`, [...leet].reverse().join(''),
    word.split('').map((char, i) => i % 2 ? char.toUpperCase() : char).join(''));
}
add('leet/case/reverse', 'l|1!i', '@4a', '0oO', '$5sS', '8bB', '7tT', '2zZ', '()password', '|<night', '|)ragon',
  '|\/|onkey', '\/\/elcome', 'p455w0rd', 'p@$$w()rd', 'drowssap', 'Drowssap', 'DR0WSS@P');

for (const separator of [' ', ',', ';', ':', '|', '/', '\\', '_', '.', '-', '!', '@', '#', '$', '+', '=', '😀']) {
  add('separators/passphrases', ['correct', 'horse', 'battery', 'staple'].join(separator),
    ['red', 'green', 'blue'].join(separator), ['one', 'two', 'three'].join(separator),
    ['alpha', 'bravo', 'charlie'].join(separator), `wallet${separator}password${separator}2026`);
}
for (const [name, words] of Object.entries(dictionary)) {
  if (!name.endsWith('-en') || words.length > 26) continue;
  add('word-sequences', words.slice(0, 3).join(''), words.slice(0, 4).join(' '), words.slice(1, 4).reverse().join('-'),
    words.slice(-3).join('_'), words.slice(0, 4).map(x => String(x).toUpperCase()).join('/'));
}

add('spatial', 'qwerty', 'qwertyuiop', 'QWERTYUIOP', 'qazwsx', '1qaz2wsx3edc', '!QAZ@WSX', 'asdfghjkl',
  'poiuytrewq', 'zxcvbnm', 'qwertz', 'azerty', 'aqwzsx', '123qweasd', "',.pyf", ';qjkxb', 'aoeuidhtns',
  '789456123', '741852963', '159753', '2580', '963.852', '/*-+', '123456789', '987654321', '0987654321');
for (const value of ['qwerty', 'asdfgh', 'zxcvbn', '12345', '1qaz', '7896']) {
  add('spatial', `${value}!`, value.repeat(2), `${value}2026`);
}

for (const date of ['01012026', '12312026', '31122026', '20260101', '20261231', '2026-01-01', '01/01/2026',
  '31.12.2026', '12/31/2026', '1/1/26', '1-1-99', '29/02/2024', '29/02/2025', '31/02/2026', '0/0/2026',
  '1910', '1945', '1960', '1985', '1999', '2000', '2001', '2010', '2020', '2026', '2030', '2050', '2051',
  '1000', '999', '010199', '123199', '991231', '202612', '120126', '26-11-04', '04/11/2026']) {
  add('date/recent-year', date, `secret${date}`);
}
for (const base of ['a', 'ab', 'abc', 'aba', 'aab', '123', 'qwerty', 'password', 'abc1', '🐈', 'é', 'Aa!', 'a_a']) {
  add('repeat', base.repeat(2), base.repeat(3), base.repeat(7));
}
add('sequences', 'abcdef', 'fedcba', 'abcdcba', 'acegik', 'zyxwvu', 'ABCDEF', '246810', '97531',
  'abcdefghijklmno', 'ABCDEFGHIJKLMNOP', '0123456789ABCDEF');
add('unicode/whitespace', 'café', 'CAFÉ', 'Éclair', 'éclair', 'Straße', 'STRASSE', 'İstanbul', 'ıstanbul',
  'Αβγδεζη', 'пароль', 'ПАРОЛЬ', '密码密码', 'こんにちは世界', 'שלום', 'مرحبا', '💚🔐🌍🚀', 'éééééé',
  'e\u0301e\u0301e\u0301', 'password\u00a0wallet', ' password ', 'password\twallet', 'password\nwallet',
  'wallet\r\npassword', '\u0000', 'pass\u0000word', 'a\u2028b\u2029c', 'ß', 'ẞ', 'İ', 'ı', 'ſ', 'K',
  '\ud800', '\udc00', '\ud800password', '😀abc😀', 'ÄÖÜäöüß', 'ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞ');

let seed = 0x4154544f;
const random = () => { seed ^= seed << 13; seed ^= seed >>> 17; seed ^= seed << 5; return (seed >>> 0) / 0x100000000; };
const randomString = (length, alphabet) => Array.from({ length }, () => alphabet[Math.floor(random() * alphabet.length)]).join('');
for (let i = 0; i < 120; i++) {
  const alphabet = ['abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789',
    'abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{};:,.?'][i % 3];
  add('seeded/random', randomString(5 + i % 28, alphabet));
}
for (const length of [14, 15, 16, 255, 256, 257, 258]) {
  add('length/UTF16-boundaries', 'x'.repeat(length), 'password'.repeat(Math.ceil(length / 8)).slice(0, length),
    randomString(length, 'bfhjkmpqvwxz2346789'));
}
add('length/UTF16-boundaries', '🐈'.repeat(127) + 'a', '🐈'.repeat(128), '🐈'.repeat(129),
  'x'.repeat(255) + '😀', 'x'.repeat(256) + 'different suffix', 'x'.repeat(255) + 'a',
  'abcdEFGH1234!@#', 'abcdEFGH1234!@#$', 'a-b-c-d-e-f-g-h-i', '   '.repeat(85) + ' ');

// Upstream uses plain objects for ranked dictionaries: inherited values also
// participate in its word-sequence lookup. Freeze those quirks rather than
// replacing the oracle's lookup with own-property or Map semantics.
for (const token of ['constructor', '__proto__']) {
  const leet = token.replaceAll('o', '0').replaceAll('s', '$').replaceAll('t', '7');
  const reverse = token.split('').reverse().join('');
  for (const variant of [token, token.toUpperCase(), token[0].toUpperCase() + token.slice(1),
    token.split('').map((char, i) => i % 2 ? char.toUpperCase() : char).join(''), leet, reverse]) {
    add('prototype-lookup', variant, variant.repeat(2), variant.repeat(3), `${variant}one`, `one${variant}`,
      `atto${variant}wallet`, `${variant}attowallet`, `attocash${variant}`);
  }
  for (const separator of ['', ' ', ',', ';', ':', '|', '/', '\\', '_', '.', '-', '!', '\t', '\u00a0']) {
    add('prototype-lookup', [token, token].join(separator), [token, 'one', 'two'].join(separator),
      ['first', token, 'second'].join(separator), ['atto', token, 'wallet'].join(separator));
  }
  for (const length of [14, 15, 16, 255, 256, 257]) {
    const filler = 'x'.repeat(Math.max(0, length - token.length));
    add('prototype-lookup', filler + token, token + filler);
  }
}
for (const [name, words] of Object.entries(dictionary)) {
  if (!name.endsWith('-en') || words.length > 26) continue;
  add('prototype-word-sequences', `constructor${words[0]}${words[1]}`, `${words[0]}__proto__${words[1]}`,
    `${words[0]} constructor ${words[1]}`, `${words[0]}-__proto__-${words[1]}`);
}
add('prototype-lookup', 'constructor__proto__', '__proto__constructor', 'constructor__proto__constructor',
  '__proto__constructor__proto__', 'CONSTRUCTOR__PROTO__', '__PROTO__CONSTRUCTOR', 'c0nstruct0r__pr0t0__',
  'toStringtoString', 'valueOfvalueOf', 'hasOwnPropertyhasOwnProperty', '__defineGetter____defineGetter__',
  '__defineSetter____defineSetter__', 'isPrototypeOfisPrototypeOf', 'propertyIsEnumerablepropertyIsEnumerable',
  'toLocaleStringtoLocaleString', '__lookupGetter____lookupGetter__', '__lookupSetter____lookupSetter__');

// Case expansion, context-dependent case mappings, supplementary characters,
// combining marks and ECMAScript/non-ECMAScript whitespace stay unnormalized.
for (const word of ['İSTANBUL', 'I\u0307STANBUL', 'ΣΟΣ', 'ΟΣ', 'ΣΣ', 'ΣΟΣΣ', 'ὈΔΥΣΣΕΎΣ', 'ẞTRAẞE',
  'KELVIN', 'ſecret', 'ǅURO', 'ǈUBICA', 'ﬀpassword', 'ﬃwallet', 'ԱԲԳԴԵ', 'ᲐᲑᲒ', '𐐀𐐁𐐂', '𐒰𐒱𐒲']) {
  add('unicode/case-expansion', word, word.toLowerCase(), `${word}password`, `wallet${word}`, word.repeat(2));
}
for (const whitespace of ['\u000b', '\u000c', '\u0085', '\u00a0', '\u1680', '\u180e', '\u2000', '\u2001',
  '\u2002', '\u2003', '\u2004', '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200a', '\u200b',
  '\u2028', '\u2029', '\u202f', '\u205f', '\u2060', '\u3000', '\ufeff']) {
  add('unicode/exotic-whitespace', `one${whitespace}two${whitespace}three`, `atto${whitespace}wallet`,
    `${whitespace}password${whitespace}`);
}

const kotlinString = value => {
  let literal = '"';
  // Preserve each UTF-16 code unit while keeping generated source ASCII-only.
  // This also prevents formatters from treating an embedded BOM as file syntax.
  for (let index = 0; index < value.length; index++) {
    const character = value[index];
    if (character === '"') literal += '\\"';
    else if (character === '\\') literal += '\\\\';
    else if (character === '$') literal += '\\$';
    else {
      const codeUnit = value.charCodeAt(index);
      literal += codeUnit < 32 || codeUnit > 126
        ? `\\u${codeUnit.toString(16).padStart(4, '0')}` : character;
    }
  }
  return `${literal}"`;
};
const kotlinDouble = value => {
  assert(Number.isFinite(value));
  const literal = String(value);
  return /[.eE]/.test(literal) ? literal : `${literal}.0`;
};
const counts = {};
const scores = {};
const rows = [];
for (const [password, group] of passwords) {
  const result = estimator.check(password, context);
  counts[group] = (counts[group] ?? 0) + 1;
  scores[result.score] = (scores[result.score] ?? 0) + 1;
  rows.push(`        PasswordReferenceCase(${kotlinString(password)}, ${result.score}, ${kotlinDouble(result.guesses)}),`);
}
assert(rows.length >= 1100);
assert(counts['prototype-lookup'] + counts['prototype-word-sequences'] >= 80);
const output = `package cash.atto.wallet.password\n\n// Generated by scripts/generate-password-strength-reference.mjs: core 4.2.0, common 4.1.3, English 4.1.1.\n// Reference year: 2026; maxLength: 256 UTF-16 code units; user inputs: ${JSON.stringify(context)}.\ninternal data class PasswordReferenceCase(val password: String, val score: Int, val guesses: Double)\n\ninternal object PasswordReferenceCases {\n    val cases: List<PasswordReferenceCase> = listOf(\n${rows.join('\n')}\n    )\n}\n`;
assert([...output].every(character => character.charCodeAt(0) <= 127), 'Generated Kotlin must remain ASCII-only.');
const destination = path.join(root, 'composeApp/src/commonTest/kotlin/cash/atto/wallet/password/PasswordReferenceCases.kt');
fs.writeFileSync(destination, output);
console.log(JSON.stringify({ cases: rows.length, scores, groups: counts,
  sha256: crypto.createHash('sha256').update(output).digest('hex') }, null, 2));

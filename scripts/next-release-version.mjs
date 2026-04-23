import { appendFile } from "node:fs/promises";
import { spawnSync } from "node:child_process";

const command = [
  "npx",
  "--yes",
  "-p",
  "semantic-release@24.2.7",
  "-p",
  "@semantic-release/commit-analyzer@13.0.1",
  "-p",
  "@semantic-release/release-notes-generator@14.1.0",
  "semantic-release",
  "--dry-run",
  "--no-ci",
];

const result = spawnSync(command[0], command.slice(1), {
  cwd: process.cwd(),
  env: process.env,
  encoding: "utf8",
});

process.stdout.write(result.stdout ?? "");
process.stderr.write(result.stderr ?? "");

if (result.status !== 0) {
  process.exit(result.status ?? 1);
}

const output = `${result.stdout ?? ""}\n${result.stderr ?? ""}`;
const versionMatch = output.match(/The next release version is ([0-9A-Za-z.+-]+)/);
const tagMatch = output.match(/Published release ([0-9A-Za-z.+-]+)/);

const version = versionMatch?.[1] ?? "";
const tag = tagMatch?.[1] ?? (version ? version : "");
const hasRelease = Boolean(version);

if (process.env.GITHUB_OUTPUT) {
  await appendFile(
    process.env.GITHUB_OUTPUT,
    `has_release=${hasRelease}\nversion=${version}\ntag=${tag}\n`,
  );
}

if (hasRelease) {
  console.log(`Next release: ${version}`);
} else {
  console.log("No release will be published.");
}

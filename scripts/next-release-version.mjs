import semanticRelease from "semantic-release";

const result = await semanticRelease(
  {
    branches: ["main"],
    tagFormat: "${version}",
    dryRun: true,
    ci: false,
    plugins: [
      "@semantic-release/commit-analyzer",
      "@semantic-release/release-notes-generator",
    ],
  },
  {
    cwd: process.cwd(),
    env: process.env,
    stdout: process.stdout,
    stderr: process.stderr,
  },
);

const hasRelease = Boolean(result?.nextRelease?.version);
const version = hasRelease ? result.nextRelease.version : "";
const tag = hasRelease ? result.nextRelease.gitTag : "";

if (process.env.GITHUB_OUTPUT) {
  const output = await import("node:fs/promises");
  await output.appendFile(
    process.env.GITHUB_OUTPUT,
    `has_release=${hasRelease}\nversion=${version}\ntag=${tag}\n`,
  );
}

if (hasRelease) {
  console.log(`Next release: ${version}`);
} else {
  console.log("No release will be published.");
}

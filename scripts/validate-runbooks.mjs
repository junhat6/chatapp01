#!/usr/bin/env node
/**
 * Validate runbook markdown files' YAML front matter.
 * - Checks presence of front matter at top delimited by --- ... ---
 * - Requires keys: id, title, scope, env, duration, rollback, impact
 * - Ensures unique id across all runbooks
 * - Prints errors and exits with code 1 on failure
 */

import fs from 'node:fs';
import path from 'node:path';

const ROOT = process.cwd();
const TARGET_DIRS = [path.join(ROOT, 'docs')];
const REQUIRED_KEYS = ['id', 'title', 'scope', 'env', 'duration', 'rollback', 'impact'];

/** Recursively collect markdown files */
function collectMarkdownFiles(dirPath) {
  const entries = fs.readdirSync(dirPath, { withFileTypes: true });
  const files = [];
  for (const entry of entries) {
    if (entry.name.startsWith('.')) continue;
    const fullPath = path.join(dirPath, entry.name);
    if (entry.isDirectory()) {
      files.push(...collectMarkdownFiles(fullPath));
    } else if (entry.isFile() && entry.name.endsWith('.md')) {
      files.push(fullPath);
    }
  }
  return files;
}

/** Extract front matter string (without the --- lines) */
function extractFrontMatter(content) {
  if (!content.startsWith('---')) return null;
  const endIdx = content.indexOf('\n---', 3);
  if (endIdx === -1) return null;
  const fmBlock = content.slice(3, endIdx).replace(/^\n/, '');
  return fmBlock;
}

function hasKey(frontMatter, key) {
  const re = new RegExp(`^${key}:[\t\x20].+`, 'm');
  return re.test(frontMatter);
}

function extractId(frontMatter) {
  const m = frontMatter.match(/^id:[\t\x20]*([^\n#]+)/m);
  return m ? m[1].trim() : null;
}

function validateFile(filePath) {
  const content = fs.readFileSync(filePath, 'utf8');
  const fm = extractFrontMatter(content);
  const errors = [];

  if (!fm) {
    errors.push('missing front matter (---) at top');
    return { filePath, errors, id: null };
  }

  for (const key of REQUIRED_KEYS) {
    if (!hasKey(fm, key)) {
      errors.push(`missing required key: ${key}`);
    }
  }

  const id = extractId(fm);
  if (!id) {
    errors.push('unable to parse id');
  }

  return { filePath, errors, id };
}

function main() {
  const mdFiles = TARGET_DIRS.flatMap(collectMarkdownFiles).filter(p => /\.md$/.test(p));
  // Only runbooks: include docs/ and docs/runbooks/
  const runbookFiles = mdFiles.filter(p => p.includes(path.sep + 'docs' + path.sep));

  const results = runbookFiles.map(validateFile);
  const idSet = new Set();
  const idDupErrors = [];

  for (const r of results) {
    if (!r.id) continue;
    if (idSet.has(r.id)) {
      idDupErrors.push(`duplicate id: ${r.id}`);
    } else {
      idSet.add(r.id);
    }
  }

  const hasErrors = results.some(r => r.errors.length > 0) || idDupErrors.length > 0;

  if (hasErrors) {
    console.error('Runbook validation failed.');
    for (const r of results) {
      if (r.errors.length > 0) {
        console.error(`\n[${path.relative(ROOT, r.filePath)}]`);
        for (const e of r.errors) console.error(`- ${e}`);
      }
    }
    if (idDupErrors.length > 0) {
      console.error(`\n[global]`);
      for (const e of idDupErrors) console.error(`- ${e}`);
    }
    process.exit(1);
  }

  console.log('Runbook validation passed.');
  for (const r of results) {
    console.log(`- ok: ${path.relative(ROOT, r.filePath)} (id=${r.id})`);
  }
}

main(); 
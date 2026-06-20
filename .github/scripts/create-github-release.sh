#!/usr/bin/env bash
set -euo pipefail

version="${1:-${VERSION_NAME:-}}"
version="${version#v}"

if [[ -z "$version" ]]; then
  echo "VERSION_NAME or a version argument is required." >&2
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required to create releases." >&2
  exit 1
fi

tag="v${version}"
title="GeoQibla ${tag}"
target="${RELEASE_TARGET:-${GITHUB_SHA:-}}"
notes_file="$(mktemp)"

cleanup() {
  rm -f "$notes_file"
}
trap cleanup EXIT

if [[ -f CHANGELOG.md ]]; then
  awk -v version="$version" '
    $0 ~ "^##[[:space:]]+" version "([[:space:]]|$)" {
      in_section = 1
      next
    }
    in_section && $0 ~ "^##[[:space:]]+" {
      exit
    }
    in_section {
      print
    }
  ' CHANGELOG.md > "$notes_file"
fi

create_args=("$tag" "--title" "$title")
if [[ -n "$target" ]]; then
  create_args+=("--target" "$target")
fi

if [[ -s "$notes_file" ]]; then
  if gh release view "$tag" >/dev/null 2>&1; then
    gh release edit "$tag" \
      --title "$title" \
      --notes-file "$notes_file"
  else
    gh release create "${create_args[@]}" --notes-file "$notes_file"
  fi
else
  if gh release view "$tag" >/dev/null 2>&1; then
    gh release edit "$tag" --title "$title"
  else
    gh release create "${create_args[@]}" --generate-notes
  fi
fi

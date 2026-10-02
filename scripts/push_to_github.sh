#!/bin/bash
set -e

echo "=== GitHub Authentication ==="
gh auth login -p https -w -s repo

echo "=== Setting up git credentials ==="
gh auth setup-git

USERNAME=$(gh api user -q .login)
echo "Logged in as GitHub user: $USERNAME"

REPO_NAME="spine-android"
REMOTE_URL="https://github.com/$USERNAME/$REPO_NAME.git"

git remote remove origin 2>/dev/null || true
git remote add origin "$REMOTE_URL"

echo "=== Pushing to $REMOTE_URL ==="
git branch -M main
git push -u origin main --force

echo "=== PUSH SUCCESSFUL ==="
echo "Repository: https://github.com/$USERNAME/$REPO_NAME"
echo "GitHub Actions Build: https://github.com/$USERNAME/$REPO_NAME/actions"

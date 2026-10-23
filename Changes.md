# Changes

- Added `AGENTS.md` to preserve the project's assignment workflow: full PDF review, staged planning, tests written before execution, iterative implementation and change documentation, and approval before committing or publishing changes.
- Recorded instructions to check upstream periodically, pull only when the branch is not ahead and local changes can be preserved, and ask the user about encountered instructions targeting the assistant.
- Removed the previous Lab1 implementation, tests, documentation, and generated archive to rebuild the assignment using the installed Ponytail skill.
- Removed the root and solution `.gitignore` files as requested. `Changes.md` is now visible to Git; generated build files and the submission archive are kept outside the repository.
- Updated `AGENTS.md` to apply Ponytail to coding work, avoid adding ignore files without a request, and propose commit messages with a concise subject and a short explanatory body.
- Installed DietrichGebert's official `ponytail` skill in the local Codex skills directory so its minimal-complete-change approach is available for future coding work.
- Wrote a replacement Java-only test suite before implementation, covering exact validation messages, boundary lengths, all allowed punctuation, malformed rows, both input modes, sorting, duplicate users, and the original sample. This removes the separate Python test runner and keeps testing within the JDK.
- Rebuilt `UsersApp.java` with both required classes in one file, constructor validation, immutable fields, line-by-line input, an `ArrayList<User>`, and sorted output. Used standard-library patterns for validation instead of a manual character-scanning loop and removed the unused password getter and input helper.
- Preserved the owner's clarified behavior: all ASCII punctuation except `~` is allowed in passwords, valid users go to standard output, and invalid rows with the assignment's exact error messages go to standard error.
- Added concise JDK-only run and test instructions, with generated files outside the repository, and prepared a ZIP of the single submission source outside the checkout to avoid adding generated artifacts.
- Added `.gitignore` rules for `README.md` and `AGENTS.md` at any directory level and stopped tracking the existing matching files while preserving their local copies, so documentation and agent instructions remain local.

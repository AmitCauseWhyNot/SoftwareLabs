# Software Engineering Labs

Each lab is maintained on its own branch. `main` contains shared repository
documentation and configuration; lab branches retain their `LabN/` directory.

| Branch | Contents |
| --- | --- |
| `main` | Shared documentation and configuration |
| `lab1` | Lab 1 assignment, input/output samples, Java source, and tests |
| `lab2` | Lab 2 assignment, input sample, Maven project, JavaFX views, and tests |

Commit your current work before switching branches:

```powershell
git switch lab1
git switch lab2
```

Start each new lab from the shared `main` branch, rather than another lab:

```powershell
git switch main
git switch -c lab3
```

Put the new assignment and implementation in `Lab3/` (use the corresponding
number for later labs). Keep lab-specific work on its lab branch rather than
merging it into `main`. Earlier commits remain available in Git history.

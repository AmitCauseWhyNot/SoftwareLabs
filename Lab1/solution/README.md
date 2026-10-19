# Lab 1

`UsersApp.java` contains both required classes in the single source file used for
submission. `User` is immutable and rejects invalid credentials in its constructor.
The application reads rows into an `ArrayList<User>` and prints them sorted by
username, preserving duplicates. No external libraries are required.

From the repository root in PowerShell, using JDK 8 or newer:

```powershell
$labBuild = Join-Path $env:TEMP 'SoftwareLabs-Lab1'
New-Item -ItemType Directory -Force -Path $labBuild | Out-Null
javac -encoding UTF-8 -d $labBuild Lab1/solution/UsersApp.java
java -cp $labBuild UsersApp Lab1/Users.txt.txt
```

The optional argument names a UTF-8 input file. Without it, the program reads
standard input, allowing the redirection described in the assignment. Valid
users go to standard output; invalid rows and their exact assignment error
messages go to standard error, as agreed with the project owner. Input errors
do not stop subsequent rows. File read failures and incorrect argument counts
exit with status 1.

Usernames are limited to 50 characters and use the assignment's three-part email
format: a nonempty local part allowing ASCII letters, digits, and `._%+-`; a domain
starting with a letter or digit and allowing letters, digits, dots, and hyphens;
and a final suffix of at least two ASCII letters. Passwords have 8–12 characters
and require an ASCII letter, digit, and punctuation character. All ASCII
punctuation except `~` is allowed, following the owner's clarification. The
username is validated before the password, with length checked before format.
Blank rows, missing fields, and extra fields are reported as invalid credentials.

Compile and run the tests with the same temporary build directory:

```powershell
javac -encoding UTF-8 -cp $labBuild -d $labBuild Lab1/solution/UsersAppTest.java
java -cp $labBuild UsersAppTest Lab1
```

The tests cover validation boundaries, exact error messages, every allowed ASCII
punctuation character, sorting, duplicates, empty/malformed input, whitespace,
both input modes, file failures, and the original sample. They normalize platform
line endings when comparing output. Build files and test output stay outside the
repository; no `.gitignore` is needed. Submit a ZIP containing only `UsersApp.java`.

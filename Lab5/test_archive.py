import sqlite3,sys
from zipfile import ZipFile
with ZipFile(sys.argv[1]) as archive:
    names = ['init.sql'] + [f'q{i:02}_solution.sql' for i in range(1,18)]
    assert sorted(archive.namelist()) == sorted(names)
    db = sqlite3.connect(':memory:')
    db.execute('PRAGMA foreign_keys=ON')
    db.executescript(archive.read(names[0]).decode())
    def query(i):
        return db.execute(archive.read(names[i]).decode()).fetchall()
    assert [len(query(i)) for i in range(1,18)] == [10,2,5,1,1,1,10,4,4,7,10,5,5,2,4,0,5]
    assert query(4) == [('Keyboard',)] and query(5) == [('Laptop',)]
    assert query(6) == [(273,)]
    assert [r[-1] for r in query(12)] == [7,6,3,3,2]
    db.execute("INSERT INTO Orders VALUES(311,201,1,'2024-02-29',1)")
    assert query(16) == [('Alice Cohen',)] and len(query(10)) == 7
    print('Archive and 17 query results verified')

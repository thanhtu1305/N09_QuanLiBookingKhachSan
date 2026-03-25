# N09_HotelPMS

## Ten de tai
He thong quan li dat phong khach san (Java Desktop)

## Cong nghe su dung
- Java Core
- Java Swing
- JDBC
- IntelliJ IDEA

## Cau truc thu muc
```text
N09_HotelPMS/
├─ src/
│  ├─ dao/
│  ├─ db/
│  ├─ entity/
│  ├─ gui/
│  ├─ images/
│  ├─ utils/
│  └─ Main.java
├─ database/
│  ├─ schema.sql
│  ├─ seed.sql
│  └─ README.md
├─ docs/
│  └─ README.md
├─ lib/
├─ .gitignore
└─ README.md
```

## Mo ta nhanh
- `src/entity`: cac lop thuc the (model)
- `src/dao`: cac lop truy cap du lieu
- `src/gui`: cac man hinh giao dien Swing
- `src/db`: ket noi CSDL
- `src/images`: tai nguyen anh cho giao dien
- `src/utils`: cac lop tien ich
- `database/`: script SQL khoi tao CSDL va du lieu mau
- `docs/`: tai lieu bo sung cua do an
- `lib/`: thu vien `.jar` duoc them thu cong

## Cau hinh SQL Server
- Project ket noi SQL Server bang JDBC qua `src/db/ConnectDB.java`.
- Cau hinh mac dinh:
  - `DB_HOST=localhost`
  - `DB_PORT=1433`
  - `DB_NAME=QLKS`
  - `DB_USER=sa`
  - `DB_PASSWORD=123456`
- Co the doi cau hinh theo 2 cach:
  - Dat bien moi truong Windows theo file mau `db.example.properties`
  - Hoac them VM options trong IntelliJ, vi du:
    `-DDB_HOST=localhost -DDB_PORT=1433 -DDB_NAME=QLKS -DDB_USER=sa -DDB_PASSWORD=your_password`
- JDBC URL duoc tao theo mau:
  `jdbc:sqlserver://<host>:<port>;databaseName=<db>;encrypt=true;trustServerCertificate=true`
- Can add SQL Server JDBC driver vao module dependency, vi du `mssql-jdbc-12.x.x.jre11.jar`.

## Luu y khi dua len GitHub
- Khong dua `out/`, `.idea/`, `*.iml`, `*.class` len repo
- Neu da tung commit cac file build, can remove khoi Git tracking truoc khi push
- Project hien tai khong dung Maven/Gradle, mo truc tiep bang IntelliJ va gan thu vien trong `lib/`

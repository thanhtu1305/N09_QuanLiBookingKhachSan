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

## Luu y khi dua len GitHub
- Khong dua `out/`, `.idea/`, `*.iml`, `*.class` len repo
- Neu da tung commit cac file build, can remove khoi Git tracking truoc khi push
- Project hien tai khong dung Maven/Gradle, mo truc tiep bang IntelliJ va gan thu vien trong `lib/`

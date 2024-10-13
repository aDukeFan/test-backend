create table author
(
    id     serial primary key,
    full_name varchar(255) not null,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table budget
(
    id     serial primary key,
    year   int  not null,
    month  int  not null,
    amount int  not null,
    type   text not null,
    author_id int,
    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES author(id)
);
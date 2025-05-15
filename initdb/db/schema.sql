create table tbl_permission
(
    created_at  timestamp(6),
    updated_at  timestamp(6),
    id          uuid not null
        primary key,
    category    varchar(255),
    description varchar(255),
    path        varchar(255)
);

alter table tbl_permission
    owner to postgres;

create table tbl_request_type
(
    created_at        timestamp(6),
    updated_at        timestamp(6),
    id                uuid         not null
        primary key,
    request_type_name varchar(255) not null
        unique
);

alter table tbl_request_type
    owner to postgres;

create table tbl_role
(
    created_at  timestamp(6),
    updated_at  timestamp(6),
    id          uuid not null
        primary key,
    description varchar(255),
    name        varchar(255)
);

alter table tbl_role
    owner to postgres;

create table tbl_role_has_permission
(
    created_at    timestamp(6),
    updated_at    timestamp(6),
    id            uuid not null
        primary key,
    permission_id uuid
        constraint fkimpu2q9vas7utdl3gti0w2ied
            references tbl_permission,
    role_id       uuid
        constraint fk7qnieb63nch1w822p22wu7il9
            references tbl_role
);

alter table tbl_role_has_permission
    owner to postgres;

create table tbl_route
(
    period_end   date,
    period_start date,
    created_at   timestamp(6),
    updated_at   timestamp(6),
    id           uuid not null
        primary key,
    code         varchar(255)
        unique,
    description  varchar(255),
    path         varchar(255)
);

alter table tbl_route
    owner to postgres;

create table tbl_checkpoint
(
    created_at  timestamp(6),
    updated_at  timestamp(6),
    id          uuid not null
        primary key,
    route_id    uuid
        constraint fkbmbfpb4iq7r5xqvcni40immlu
            references tbl_route,
    description varchar(255),
    latitude    varchar(255),
    longitude   varchar(255),
    name        varchar(255),
    status      checkpointstatus
);

alter table tbl_checkpoint
    owner to postgres;

create table tbl_user
(
    dob          date,
    created_at   timestamp(6),
    updated_at   timestamp(6),
    phone        varchar(15)  not null
        unique,
    id           uuid         not null
        primary key,
    address      varchar(255),
    avatar       varchar(255),
    device_token varchar(255),
    email        varchar(255),
    name         varchar(255),
    password     varchar(255),
    platform     varchar(255),
    username     varchar(255) not null
        unique,
    version_app  varchar(255),
    gender       gender,
    status       userstatus,
    type         usertype
);

alter table tbl_user
    owner to postgres;

create table tbl_assistant
(
    created_at timestamp(6),
    updated_at timestamp(6),
    id         uuid not null
        primary key,
    user_id    uuid
        unique
        constraint fkd0q0tsqkraeh4r9txe8n8tjd0
            references tbl_user
);

alter table tbl_assistant
    owner to postgres;

create table tbl_driver
(
    created_at timestamp(6),
    updated_at timestamp(6),
    id         uuid not null
        primary key,
    user_id    uuid
        unique
        constraint fklrbhtba8tjq1omqre4v0e3m7t
            references tbl_user
);

alter table tbl_driver
    owner to postgres;

create table tbl_bus
(
    amount_of_student integer,
    max_capacity      integer,
    esp_id            varchar(6)
        unique,
    created_at        timestamp(6),
    updated_at        timestamp(6),
    assistant_id      uuid
        constraint fk32dq2l5apjv42s62lsq6ubfqu
            references tbl_assistant,
    driver_id         uuid
        constraint fkiwkju59ne35v8xy4xdu7m47q3
            references tbl_driver,
    id                uuid not null
        primary key,
    route_id          uuid
        constraint fktd08wcl89f08rig26dca87lte
            references tbl_route,
    license_plate     varchar(255)
        unique,
    name              varchar(255)
        unique,
    status            busstatus
);

alter table tbl_bus
    owner to postgres;

create table tbl_bus_schedule
(
    date                date,
    created_at          timestamp(6),
    updated_at          timestamp(6),
    assistant_id        uuid
        constraint fk4ca7je04j15idjwbqkduth612
            references tbl_assistant,
    bus_id              uuid
        constraint fkdykwj3m7v549p79mq9qrfs0wx
            references tbl_bus,
    driver_id           uuid
        constraint fkavlvrvp17see1vghautfg4mvs
            references tbl_driver,
    id                  uuid not null
        primary key,
    route_id            uuid
        constraint fk5bjhkfx6jsigja97grf5j803y
            references tbl_route,
    direction           busdirection,
    bus_schedule_status busschedulestatus,
    note                text
);

alter table tbl_bus_schedule
    owner to postgres;

create table tbl_camera
(
    created_at     timestamp(6),
    time_basic     timestamp(6),
    time_heartbeat timestamp(6),
    bus_id         uuid
        unique
        constraint fkjymq5952bcpwn7o4bpjbehmnw
            references tbl_bus,
    facesluice     varchar(255) not null
        primary key
);

alter table tbl_camera
    owner to postgres;

create table tbl_camera_request
(
    created_at   timestamp(6),
    updated_at   timestamp(6),
    id           uuid not null
        primary key,
    camera_id    varchar(255)
        constraint fk6ky3genkfuoe9mgbha5l8yy6a
            references tbl_camera,
    request_type camerarequesttype,
    status       camerarequeststatus
);

alter table tbl_camera_request
    owner to postgres;

create table tbl_parent
(
    created_at timestamp(6),
    updated_at timestamp(6),
    id         uuid not null
        primary key,
    user_id    uuid
        unique
        constraint fkbw0x44omv13rn79oj29cvtws4
            references tbl_user
);

alter table tbl_parent
    owner to postgres;

create table tbl_student
(
    created_at    timestamp(6),
    dob           timestamp(6),
    updated_at    timestamp(6),
    bus_id        uuid
        constraint fkssgvp81w0dh7dwdd0adqc300j
            references tbl_bus,
    checkpoint_id uuid
        constraint fkkffwqg38imfhxf6lxidier4hp
            references tbl_checkpoint,
    id            uuid         not null
        primary key,
    parent_id     uuid
        constraint fkrth47ukrr9irr0g7rla5teksm
            references tbl_parent,
    address       varchar(255),
    avatar        varchar(255),
    name          varchar(255),
    roll_number   varchar(255) not null
        unique,
    gender        gender,
    status        studentstatus
);

alter table tbl_student
    owner to postgres;

create table tbl_camera_request_detail
(
    err_code          integer,
    person_type       integer,
    camera_request_id uuid not null
        constraint fk7qu3of2l062tjg6jk7q57b29t
            references tbl_camera_request,
    student_id        uuid not null
        constraint fkpxgcfkjrgcb00eprei3n3j181
            references tbl_student,
    avatar            varchar(255),
    name              varchar(255),
    roll_number       varchar(255),
    primary key (camera_request_id, student_id)
);

alter table tbl_camera_request_detail
    owner to postgres;

create table tbl_request
(
    created_at      timestamp(6),
    updated_at      timestamp(6),
    approved_by     uuid
        constraint fkj5x0ui1avlh244mp97dadxc7t
            references tbl_user,
    checkpoint_id   uuid
        constraint fkrmm9fibbfqnegr1jm4k4oqq8t
            references tbl_checkpoint,
    id              uuid not null
        primary key,
    request_type_id uuid
        constraint fk3lt26kvfomrd7iechkv10ddga
            references tbl_request_type,
    send_by         uuid
        constraint fkkl7fyayc77m7ibpr4rxfouy1y
            references tbl_user,
    student_id      uuid
        constraint fkj92qorpmjtbqr84yc12279dx
            references tbl_student,
    reason          text,
    reply           text,
    status          requeststatus,
    from_date       date,
    to_date         date
);

alter table tbl_request
    owner to postgres;

create table tbl_teacher
(
    created_at timestamp(6),
    updated_at timestamp(6),
    id         uuid not null
        primary key,
    user_id    uuid
        unique
        constraint fk1hk3w1vvwp2qrnfn45k76sqt2
            references tbl_user
);

alter table tbl_teacher
    owner to postgres;

create table tbl_attendance
(
    date          date,
    checkin       timestamp(6),
    checkout      timestamp(6),
    created_at    timestamp(6),
    updated_at    timestamp(6),
    bus_id        uuid
        constraint fkirfa28ia1ympn1e1esl3h6mnb
            references tbl_bus,
    checkpoint_id uuid
        constraint fkj4b4ki0dc2xtjccjr3hq8g0uw
            references tbl_checkpoint,
    id            uuid not null
        primary key,
    student_id    uuid
        constraint fkeeb551hswd2yhl6xd8u9t0ymu
            references tbl_student,
    teacher_id    uuid
        constraint fkk1ms69cek4xtg4nrsui9hysry
            references tbl_teacher,
    assigned_to   varchar(255),
    modified_by   varchar(255),
    direction     busdirection,
    status        attendancestatus
);

alter table tbl_attendance
    owner to postgres;

create table tbl_grade
(
    created_at timestamp(6),
    updated_at timestamp(6),
    id         uuid not null
        primary key,
    teacher_id uuid
        unique
        constraint fkayhb7agdp34e3atpi6gq7gv6x
            references tbl_teacher,
    name       varchar(255)
);

alter table tbl_grade
    owner to postgres;

create table tbl_student_has_grade
(
    created_at   timestamp(6),
    period_end   timestamp(6),
    period_start timestamp(6),
    updated_at   timestamp(6),
    grade_id     uuid
        constraint fkf73apankgvwer8xmyv6lmtmrr
            references tbl_grade,
    id           uuid not null
        primary key,
    student_id   uuid
        constraint fk5m1fxj476um54bifkxe4x7oo4
            references tbl_student
);

alter table tbl_student_has_grade
    owner to postgres;

create table tbl_teacher_has_grade
(
    created_at   timestamp(6),
    period_end   timestamp(6),
    period_start timestamp(6),
    updated_at   timestamp(6),
    grade_id     uuid
        constraint fkm5huwffg17sulvq4cwi39nfro
            references tbl_grade,
    id           uuid not null
        primary key,
    teacher_id   uuid
        constraint fkfox31hwubmiepiqui0ggpk52e
            references tbl_teacher
);

alter table tbl_teacher_has_grade
    owner to postgres;

create table tbl_token
(
    expired    boolean,
    revoked    boolean,
    created_at timestamp(6),
    updated_at timestamp(6),
    id         uuid not null
        primary key,
    user_id    uuid
        constraint fk5tf1bl1tf25n5w578v95ttn7v
            references tbl_user,
    token      varchar(255),
    token_type tokentype
);

alter table tbl_token
    owner to postgres;

create table tbl_user_has_role
(
    created_at timestamp(6),
    updated_at timestamp(6),
    id         uuid not null
        primary key,
    role_id    uuid
        constraint fknbcb2rhv9gavk1vtounetntw0
            references tbl_role,
    user_id    uuid
        constraint fklndw06guu8xedftiuosbtbr0a
            references tbl_user
);

alter table tbl_user_has_role
    owner to postgres;

create table tbl_otp_token
(
    id         uuid         not null
        primary key,
    email      varchar(256) not null,
    otp        varchar(10)  not null,
    expire_at  timestamp    not null,
    created_at timestamp,
    updated_at timestamp
);

alter table tbl_otp_token
    owner to postgres;

create table tbl_password_reset_session
(
    id         uuid        not null
        primary key,
    email      varchar(20) not null,
    expire_at  timestamp   not null,
    created_at timestamp,
    updated_at timestamp
);

alter table tbl_password_reset_session
    owner to postgres;


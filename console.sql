-- From Moodle "Portfolio 3 database" - Mads Rosendahl
drop table if exists flow;
drop table if exists transport;
drop table if exists vessel;
drop table if exists harbour;

pragma foreign_keys = ON;

create table harbour(id Integer primary key, name text UNIQUE NOT NULL check(harbour.name!=''));
insert into harbour values(1,'Jawarharlal Nehru');
insert into harbour values(2,'Tanjung Pelepas');
insert into harbour values(3,'Dar Es Salaam');
insert into harbour values(4,'Mombasa');
insert into harbour values(5,'Zanzibar');
insert into harbour values(6,'Jebel Ali Dubai');
insert into harbour values(7,'Salalah');

create table vessel(id Integer primary key, name Text NOT NULL, capacity integer check (capacity < 100000 and capacity >= 0));
insert into vessel values(1,'Maren',12000);
insert into vessel values(2,'Misse',5000);
insert into vessel values(3,'Mette',8000);
insert into vessel values(4,'Musse',10000);
insert into vessel values(5,'Mugge',8000);
insert into vessel values(6,'Marle',10000);
insert into vessel values(7,'Minne',10000);
insert into vessel values(8,'Maryk',10000);
insert into vessel values(9,'Melle',10000);
insert into vessel values(10,'Manna',10000);
insert into vessel values(11,'Mynte',10000);
insert into vessel values(12,'Munja',10000);

create table transport(id Integer primary key,
                       vessel Integer  references vessel(id),
                       fromharbour Integer  references harbour(id),
                       toharbour Integer references harbour(id)
);

insert into transport values (1,1,1,4);
insert into transport values (2,2,1,3);
insert into transport values (3,3,2,4);
insert into transport values (4,4,2,3);
insert into transport values (5,5,2,5);
insert into transport values (6,6,2,6);
insert into transport values (7,7,2,7);
insert into transport values (8,8,3,2);
insert into transport values (9,9,3,1);
insert into transport values (10,10,3,6);
insert into transport values (11,11,4,7);
insert into transport values (12,12,4,6);

create table flow(id Integer primary key autoincrement,
                  transport Integer references transport(id),
                  containers integer check ( containers>0)
);




insert into flow (transport, containers) values (1,2000);
insert into flow (transport, containers) values (2,2000);
insert into flow (transport, containers) values (3,5000);
insert into flow (transport, containers) values (4,3000);
insert into flow (transport, containers) values (5,2000);
insert into flow (transport, containers) values (6,7000);
insert into flow (transport, containers) values (7,7000);
insert into flow (transport, containers) values (8,5000);
insert into flow (transport, containers) values (9,3000);
insert into flow (transport, containers) values (10,2000);
insert into flow (transport, containers) values (11,2000);
insert into flow (transport, containers) values (12,500);

-- Table with flow greater than capacity
Select vessel.name, vessel.capacity, flow.containers from vessel
left outer join flow on vessel.id=flow.id
group by vessel.capacity
having capacity < containers;


select h1.name as fromport, h2.name as toport, v.name as vessel,
       Sum(f.containers) as flow, v.capacity
from transport t
         inner join vessel v on t.vessel = v.id
         inner join harbour h1 on t.fromharbour = h1.id
         inner join harbour h2 on t.toharbour = h2.id
         left outer join flow f on t.id = f.transport
group by t.id;

-- Table that checks if transports occur more than once pr day
select vessel.name, count(transport.id) as numberOfTransportPrDay  from vessel
left outer join transport on vessel.id = transport.id
group by vessel.name
having numberOfTransportPrDay>1;

-- Updating the existing container values
update vessel set capacity = capacity - flow.containers from flow where vessel.id = flow.id;






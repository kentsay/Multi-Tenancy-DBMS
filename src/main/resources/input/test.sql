CREATE TABLE Employees /*@shared*/(
	ttid int /*@comparable*/,
	employee_name varchar(50) /*@comparable*/,
	role_id int /*@specific*/,
	region_id int /*@comparable*/,
	salary int /*@transformable*/,
	age int /*@comparable*/,
	PRIMARY KEY (ttid, employee_name),
	FOREIGN KEY (ttid) REFERENCES Tenants (ttid),
	FOREIGN KEY (ttid, role_id) REFERENCES Roles (ttid, role_id),
	FOREIGN KEY (region_id) REFERENCES Regions (region_id)
);
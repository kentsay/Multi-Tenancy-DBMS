# MTCalc Framework

MTCalc is a Multi-Tenancy Database framework based on [Apache Calcite][https://calcite.incubator.apache.org/].

## Why it works

### Inform Database system
First we need to inform the DBMS to specify the table type(shared or tenant-specific) as well as the comparability of attributes.
This can be done by using annotations in sql comments. By parsing the sql comments, we can then translate the MT rules into a mt.json file.
 For example:
               ```
               {table: Employee,
               column: [
               {"ttid": "comparable"},{"employee_name": "comparable"},
               {"salary": "transformable", "function": "public int salary(ttid, salary) {if (ttid == 0) return salary*1.05}"}
               ]}
               ```
 Each sql query execution will then base on this mt.json file as a reference to generate the MTCalc new query.

### MT query processing
When the system receive a query, the execution steps wil be as follow:
   * Parse sql query, and check if this query matches with MT schema
       * input: {C,D,Q}
           * C: client id (ttid)
           * D: data owner (ETH, MSR)
           * Q: query statement
       * output:
           * match which kind of MTCalc operator (MTProject, MTScan, MTJoin, etc)
   * Rewrite sql query with MTCalc based on mt.json
       * MTCalc query processing
           * MTScan
           * MTProject
           * MTSelect
           * MTJoin
           * MTSet_Union
           * MTSet_Intersect
           * MTSet_Diff
           * MTGroup
           * MTOrderBy
   * Execute the new query and return result
       * Push new rule(query statement) into DBMS and receive results

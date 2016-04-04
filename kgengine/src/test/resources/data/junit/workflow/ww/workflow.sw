[] a sw:Workflow ;
  sw:debug true ;

sw:param [
  sw:test true ;
  st:status true ;
  sw:debug true
] ;

sw:loop 2 ;
sw:body ( [ a sw:Workflow ; sw:uri <workflow.ttl> ] )
.
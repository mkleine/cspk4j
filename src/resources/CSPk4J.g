/**
 *   This file is part of CSPk4J the CSP concurrency library for Java.
 *
 *   CSPk4J is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CSPk4J is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CSPk4J.  If not, see <http://www.gnu.org/licenses/>.
 *
**/
grammar CSPk4J;

options {
  language = Java;
  output=AST;
  ASTLabelType=CommonTree;
//  superClass=CspParser;
}

@header {
/**
 *   This file is part of CSPk4J the CSP concurrency library for Java.
 *
 *   CSPk4J is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CSPk4J is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CSPk4J.  If not, see <http://www.gnu.org/licenses/>.
 *
**/
  package org.cspk4j.parser;
  
  import java.util.HashMap;
  import java.util.HashSet;
  import java.util.Map;
  import java.util.Set;
  
  import org.cspk4j.*;
}

@members {
  final CspProcessStore store = new CspProcessStore();
  
  public CspProcessStore getCspProcessStore(){
    return store;
  }
  
}

@lexer::header{
/**
 *   This file is part of CSPk4J the CSP concurrency library for Java.
 *
 *   CSPk4J is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CSPk4J is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CSPk4J.  If not, see <http://www.gnu.org/licenses/>.
 *
**/
  package org.cspk4j.parser;
}

//program entry
prog
  :   stat*
  ;
  
//production of statement
stat
  : dataTypeDecl    
    | channelDecl
    | setDecl
    | processDecl
    | assertDecl
    ;

//production of datatype declaration
dataTypeDecl
  : 'datatype' IDENT '=' IDENT ('|' IDENT)*
  ;

//production of channel declaration
channelDecl
  : 'channel' IDENT 
    (','! IDENT)* (':' (setExpression|IDENT) ('.' (setExpression|IDENT))*)?
  ;
setDecl
  : IDENT '=' setExpression   
  ;
  
setExpression
  :
    '{|' (event|INT) (',' (event|INT))* '|}'
  | 
    '{' (event|INT) ((',' (event|INT))* | '..' (event|INT)) '}'
  ;

  
//production of assertions
assertDecl
  : 'assert' IDENT '[' IDENT '=' IDENT
  ;

id_map returns [Map map = new HashMap()]
  : id1=event '<-' id2=event 
      {$map.put($id1.text,$id2.text);}
      (',' id3=event '<-' id4=event
        {$map.put($id3.text,$id4.text);})*;
id_set returns [Set set = new HashSet()]
  : id1=event {$set.add($id1.text);}
    (',' id2=event {$set.add($id2.text);})*;

//production of process declaration
processDecl

  : id1=IDENT '=' (
  (
     event'->' id3=IDENT {
      store.createPrefix($id1.text,$event.text,$id3.text);}
    |
      id2=IDENT ';' id3=IDENT {
      store.createSequential($id1.text, $id2.text,$id3.text);}
    |
      id2=IDENT '|~|' id3=IDENT {
      store.createInternalChoice($id1.text,$id2.text,$id3.text);}
    |
      id2=IDENT '[]' id3=IDENT {
      store.createExternalChoice($id1.text,$id2.text,$id3.text);}
    |
      id2=IDENT '[>' id3=IDENT {
      store.createTimeout($id1.text,$id2.text,$id3.text);}
    |
      id2=IDENT '/\\' id3=IDENT {
      store.createInterrupt($id1.text,$id2.text,$id3.text);}
    |
      id2=IDENT '|||' id3=IDENT {
      store.createParallel($id1.text,$id2.text,$id3.text,java.util.Collections.<String>emptySet());}
    |
      id2=IDENT '[|' '{'anIdSet=id_set '}' '|]' id3=IDENT {
      store.createParallel($id1.text,$id2.text,$id3.text,$anIdSet.set);}
    |
      id2=IDENT '\\' '{' anIdSet=id_set '}' {
      store.createHiding($id1.text,$id2.text,$anIdSet.set);}
    |
      id2=IDENT '[[' anIdMap=id_map ']]' {
      store.createRenaming($id1.text,$id2.text,$anIdMap.map);}
    | (IDENT) => id2=IDENT
    {
      store.createAlias($id1.text,$id2.text);}

  )
  // D=[]i:{A,B,C}@i
  | '[]' IDENT ':' '{' anIdSet=id_set '}' '@' IDENT  {
    store.createExternalChoice($id1.text,$anIdSet.set);}
  | '[|' alpha=id_set '|]' IDENT ':' '{' anIdSet=id_set '}' '@' IDENT  {
    store.createParallel($id1.text,$anIdSet.set,$alpha.set);}
    | '|~|' IDENT ':' '{' anIdSet=id_set '}' '@' IDENT  {
    store.createInternalChoice($id1.text,$anIdSet.set);}
  )
  ; 

event
  : IDENT ('.' (IDENT | INT))*
  ;

IDENT : ('a'..'z'|'A'..'Z') ('0'..'9'|'a'..'z'|'A'..'Z'|'_'|'\'')*;

INT :   '0'..'9'+ ;

WS : (' ' | '\t' | '\n' | '\r' | '\f')+ {$channel = HIDDEN;};
COMMENT :'--' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;};
MULTILINE_COMMENT : '{-' .* '-}' '\n'? {$channel = HIDDEN;};
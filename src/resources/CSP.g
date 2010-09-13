grammar CSP;

options {
  language = Java;
  output=AST;
  ASTLabelType=CommonTree;
}

@header {
  package de.tub.swt.vates.csp;
  
  import java.io.PrintStream;
  import java.util.HashMap;
  import java.util.HashSet;
  import java.util.Map;
  import java.util.Set;
  
  import de.tub.swt.vates.csp.runnable.*;
}

@members {
  String packageName = "de.tub.swt.vates.csp.example";
  PrintStream out = System.out;
  public void setOutputStream(PrintStream out) {
    this.out = out;
  }
  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
  
  ProcessConfig p;
  public ProcessConfig getProcessConfig() {
    return p;
  }
}

@lexer::header{
  package de.tub.swt.vates.csp;
}

//program entry
prog
@init {
    ProcessStore.init();
    if(out!= null) {
      out.println("package "+this.packageName+";\n");
      out.println("import de.tub.swt.vates.csp.runnable.*;\n");
      out.println("final class ProcessFactoryImpl extends de.tub.swt.vates.csp.ProcessFactory {\n");
      out.println("\tprotected ProcessConfig createProcessConfig() {");
    }
}
  :   stat* {    if(out != null) out.println("\t\treturn p;\n\t}\n}");
    ProcessStore.reset();}
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
@init {
  if(out != null)
    out.print("\t\tp = ");
} 
  : id1=IDENT '=' (
  (
     event'->' id3=IDENT {
      if(out != null)
        out.println("new PrefixConfig(\""+$id1.text+"\",\""+$event.text+"\",\""+$id3.text+"\");");
      else p = new PrefixConfig($id1.text,$event.text,$id3.text);}
    |
      id2=IDENT ';' id3=IDENT {
      if(out != null)
        out.println("new SequentialConfig(\""+$id1.text+"\",\""+$id2.text+"\",\""+$id3.text+"\");");
      else p = new SequentialConfig($id1.text, $id2.text,$id3.text);}
    |
      id2=IDENT '|~|' id3=IDENT {
      if(out != null)
        out.println("new InternalChoiceConfig(\""+$id1.text+"\",\""+$id2.text+"\",\""+$id3.text+"\");");
      else p = new InternalChoiceConfig($id1.text,$id2.text,$id3.text);}
    |
      id2=IDENT '[]' id3=IDENT {
      if(out != null)
        out.println("new ExternalChoiceConfig(\""+$id1.text+"\",\""+$id2.text+"\",\""+$id3.text+"\");");
      else p = new ExternalChoiceConfig($id1.text,$id2.text,$id3.text);}
    |
      id2=IDENT '[>' id3=IDENT {
      if(out != null)
        out.println("new TimeoutConfig(\""+$id1.text+"\",\""+$id2.text+"\",\""+$id3.text+"\");");
      else p = new TimeoutConfig($id1.text,$id2.text,$id3.text);}
    |
      id2=IDENT '/\\' id3=IDENT {
      if(out != null)
        out.println("new InterruptConfig(\""+$id1.text+"\",\""+$id2.text+"\",\""+$id3.text+"\");");
      else p = new InterruptConfig($id1.text,$id2.text,$id3.text);}
    |
      id2=IDENT '|||' id3=IDENT {
      if(out != null)
        out.println("new ParallelConfig(\""+$id1.text+"\",\""+$id2.text+"\",\""+$id3.text+"\",java.util.Collections.<String>emptySet());");
      else p = new ParallelConfig($id1.text,$id2.text,$id3.text,java.util.Collections.<String>emptySet());}
    |
      id2=IDENT '[|' '{'anIdSet=id_set '}' '|]' id3=IDENT {
      if(out != null)
        out.println("new ParallelConfig(\""+$id1.text+"\",\""+$id2.text+"\",\""+$id3.text+"\",\""+$anIdSet.text+"\");");
      else p = new ParallelConfig($id1.text,$id2.text,$id3.text,$anIdSet.set);}
    |
      id2=IDENT '\\' '{' anIdSet=id_set '}' {
      if(out != null)
        out.println("new HidingConfig(\""+$id1.text+"\",\""+$id2.text+"\",\""+$anIdSet.text+"\");");
      else p = new HidingConfig($id1.text,$id2.text,$anIdSet.set);}
    |
      id2=IDENT '[[' anIdMap=id_map ']]' {
      if(out != null)
        out.println("new RenamingConfig(\""+$id1.text+"\",\""+$id2.text+"\",\""+$anIdMap.text+"\");");
      else p = new RenamingConfig($id1.text,$id2.text,$anIdMap.map);}
    | (IDENT) => id2=IDENT
    {
      if(out != null)
        out.println("new AliasConfig(\""+$id1.text+"\",\""+$id2.text+"\");");
      else p = new AliasConfig($id1.text,$id2.text);}

  )
  // D=[]i:{A,B,C}@i
  | '[]' IDENT ':' '{' anIdSet=id_set '}' '@' IDENT  {
      if(out != null)
        out.println("new ExternalChoiceConfig(\""+$id1.text+"\",\""+$anIdSet.text+"\");");
      else p = new ExternalChoiceConfig($id1.text,$anIdSet.set);}
       
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
MODULE Foo

    RECORD structure
        num field1;
        num field2;
    ENDRECORD

    ALIAS structure alias1;

    VAR alias1 field;

    PROC procedure()
        VAR num variable1;
        variable1 := 2;
    ENDPROC

ENDMODULE
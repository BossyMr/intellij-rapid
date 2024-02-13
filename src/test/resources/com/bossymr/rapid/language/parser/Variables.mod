MODULE DUMMY

    PROC routine1()
        VAR num value1;
        VAR num value2 := value1 * 2;
        CONST num value3;
        PERS bool value4;
        value4 := value3 = value2;
        value3 := 1 * 2;
    ENDPROC

    PROC routine2()
        VAR value1;
        num value2;
        CONST num value3;
    ENDPROC

ENDMODULE
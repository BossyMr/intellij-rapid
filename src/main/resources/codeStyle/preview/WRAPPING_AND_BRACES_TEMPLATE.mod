MODULE Foo(SYSMODULE, NOVIEW, NOSTEPIN, VIEWONLY, READONLY)

    VAR num array{6} := [0.1234, 12e3, 1, 2, 3, 0b0101]

    PROC Routine(num n1, num n2, num n3, num n4, num n5, num n6)
        IF n1 + 2 - 4 * 2 DIV 4 > 100 THEN
            Routine n1 - 100, n2, n3, n4, n5, n6;
        ENDIF
        TEST n1
            CASE 1:
                n1 := (1 + 2 + 3) * (2 - 3 MOD 4) * (0xAF - 0b10) + (4);
            DEFAULT:
                n1 := 2;
        ENDTEST
        ERROR
            TPWrite "ERROR";
            RETRY;
    ENDPROC

    PROC Routine2(\num n1 | num n2, x, y, z\switch On | switch Off)
    ENDPROC

ENDMODULE
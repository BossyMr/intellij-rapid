MODULE Foo

    PROC procedure(num n1, num n2, \switch On | switch Off)
        IF n1 > 2 doSomething n1;
        IF n2 > 2 THEN
             n2 := getValue(n2);
        ELSEIF Present(On) THEN
            FOR i FROM n1 TO n2 STEP 2 DO
                WHILE condition(i) DO
                    n1 := something();
                ENDWHILE
            ENDFOR
        ELSE
            TEST n1
                CASE -1, 2:
                    doSomething;
                DEFAULT:
                    doDefault;
            ENDTEST
        ENDIF
    ENDPROC

ENDMODULE
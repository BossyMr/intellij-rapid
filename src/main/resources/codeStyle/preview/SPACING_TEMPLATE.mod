MODULE Foo(SYSMODULE, READONLY)

    CONST num value{2} := [1,2];
    CONST num value2{0} := [];

    FUNC num Bar(num x, \num y | num z{*,*})
        VAR num q := 0;
        IF NOT Present(y) THEN
            IF x > 2 OR x = 3 y := x;
        ENDIF
        FOR i FROM x TO y DO
            q := -q + i * 2;
        ENDFOR
    ENDFUNC

ENDMODULE
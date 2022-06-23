MODULE SomeModule(NOSTEPIN)

        RECORD SomeRecord
        num SomeNumber;
        ENDRECORD

        ALIAS SomeRecord SomeAlias;

        ! Comment
        LOCAL VAR num field := 1;
        LOCAL VAR num unusedField := -2.3;

        VAR string globalField = "Another\\String\01\";

        VAR UnknownType field = 100e2;

        CONST num constantField = 2;
        PERS num persistentField = 3;

        FUNC num Calculate(INOUT num param1,
        \switch switch1 | switch switch2,
        num{*} reassignedParam)
        VAR num reassignedValue = constantField + param1;
        VAR num localVar = "Intellij"; ! Incompatible types
        reassignedValue := reassignedValue + field;
        IF Present(switch1) THEN
        reassignedParam := [2,3,4];
        ENDIF
        ENDFUNC

        ENDMODULE
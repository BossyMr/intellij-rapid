MODULE CalibDatas

    PERS tooldata TCP_Penna:=[TRUE,[[116,0,84],[0,0.707106781,0,0.707106781]],[0.5,[0,0,50],[1,0,0,0],0,0,0]];
    PERS tooldata TCP_Gripklo:=[TRUE,[[0,0,65],[1,0,0,0]],[0.5,[0,0,50],[1,0,0,0],0,0,0]];


    TASK PERS wobjdata Papper:=[FALSE,TRUE,"",[[422.753778935,-150.502817268,49.483818687],[0.995657022,-0.001465736,0.093085554,0.000163036]],[[0,0,0],[1,0,0,0]]];
    TASK PERS wobjdata PapperFalskt:=[FALSE,TRUE,"",[[140.405608366,420.840176368,49.483816831],[0.703917349,-0.066858495,0.064787047,0.704154064]],[[0,0,0],[1,0,0,0]]];
    TASK PERS wobjdata PennPlats:=[FALSE,TRUE,"",[[331.6705,-388.2845,42.5],[0.923879533,0,0,-0.382683432]],[[0,0,0],[1,0,0,0]]];
    TASK PERS wobjdata Klosställage:=[FALSE,TRUE,"",[[-104.982,-440.691570612,70.159247913],[0.61236742,-0.353562077,-0.353538669,-0.612380935]],[[0,0,0],[1,0,0,0]]];
    TASK PERS wobjdata Kamera:=[FALSE,TRUE,"",[[0,0,0],[1,0,0,0]],[[-0.607616,26.3416,0],[0.999848,0,0,-0.0174524]]];
ENDMODULE

        MODULE DrawModule (SYSMODULE, NOVIEW)

        RECORD bob

        num identifier;

        ENDRECORD

        RECORD other

        bob t;

        bob t2;

        bob t3;

        ENDRECORD

        CONST robtarget middle := [[75, 125, -2], [0.00, 0.992700113, 0.055506584, -0.107050994], [-1, -1, 0, 1], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]];

        CONST robtarget middle := [[75, 125, -2], [0.00, 0.992700113, 0.055506584, -0.107050994], [-1, -1, 0, 1], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]];

        CONST robtarget target := [[75, 125, -2], [0.002364117, 0.992700113, 0.055506584, -0.107050994], [-1, -1, 0, 1], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]];

        ! test1
        ! test2
        ! test3
        ! test
        ! test
        PERS num squareCount;

        PERS num triangleCount;

        PERS num circleCount;

        CONST num queue{123} := 2;

        VAR num queue2{123};

        VAR num queue3 := 1;

        CONST num value := queue;

        VAR num index := value;

        VAR num sizes{3} := [100, 50, 100];

        VAR num amounts{3} := [1, 1, 1];

        VAR robtarget targets{3} := [target, middle, target];

        VAR num buttons2{4} := [2, 2];

        VAR num buttons{4} := [2];

        PROC DrawSquare(VAR num size)
        ! test
        VAR other testing := 2;


        SafeRelease;

        testing.t3 := 5;
        queue := 2;
        SafeGrab;
        movej Offs(target, 0, 0, 0), v1000, z50, TCP_Penna\WObj := Papper;
        MoveJ Offs(target, 0, 0, 20), v1000, z10, TCP_Penna\WObj := Papper;
        MoveJ Offs(target, 0, 0, 20), v1000, z10, TCP_Penna\WObj := Papper;
        MoveL Offs(target, 0, 0, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, 0, 0, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, size, 0, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, size, size, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, 0, size, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, 0, 0, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, 0, 0, 20), v1000, z10, TCP_Penna\WObj := Papper;
        MoveJ Offs(target, 0, 0, 150), v1000, z10, TCP_Penna\WObj := Papper;
        Incr squareCount;
        ENDPROC

        !test
        ! esttest
        ! test
        !
        !
        !
        PROC DrawSquares(robtarget target, num size, num amount)
        VAR string bob;

        bob := name(size);
        FOR i FROM 1 TO amount DO
        DrawSquare target, size;
        ENDFOR
        ENDPROC

        PROC DrawCircle(robtarget target, num radius)
        SafeGrab;
        MoveJ Offs(target{1, 2}, -radius, 0, 150), v1000, z50, TCP_Penna\WObj := Papper;
        MoveJ Offs(target, -radius, 0, 20), v1000, z10, TCP_Penna\WObj := Papper;
        MoveL Offs(target, -radius, 0, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveC Offs(target, 0, radius, 0), Offs(target, radius, 0, 0), v100, fine, TCP_Penna\WObj := Papper;
        MoveC Offs(target, 0, -radius, 0), Offs(target, -radius, 0, 0), v100, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, -radius, 0, 20), v1000, z10, TCP_Penna\WObj := Papper;
        ! test
        MoveJ Offs(target, -radius, 0, 150), v1000, z10, TCP_Penna\WObj := Papper;
        Incr circleCount;
        ENDPROC

        PROC DrawCircles(robtarget target, num radius, num amount)
        FOR i FROM 1 TO amount DO
        DrawCircle target, radius;
        ENDFOR
        ENDPROC

        PROC DrawTriangle(robtarget target, \num size)
        SafeGrab;
        MoveJ Offs(target, 0, 0, 150), v1000, z50, TCP_Penna\WObj := Papper;
        MoveJ Offs(target, 0, 0, 20), v1000, z10, TCP_Penna\WObj := Papper;
        MoveL Offs(target, 0, 0, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, 0, 0, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, size, size, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, 0, size, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, 0, 0, 0), v200, fine, TCP_Penna\WObj := Papper;
        MoveL Offs(target, 0, 0, 20), v1000, z10, TCP_Penna\WObj := Papper;
        MoveJ Offs(target, 0, 0, 150), v1000, z10, TCP_Penna\WObj := Papper;
        Incr triangleCount;
        ENDPROC

        PROC DrawTriangles(robtarget target, num size, num amount)
        FOR i FROM 1 TO amount DO
        DrawTriangle target, \size := size;
        ENDFOR
        ENDPROC

        PROC Main()
        CONNECT buttons{1} WITH B1;
        ISignalDI Knapp1, 1, buttons{1};
        CONNECT buttons{2} WITH B2;
        ISignalDI Knapp2, 1, buttons{2};
        CONNECT buttons{3} WITH B3;
        ISignalDI Knapp3, 1, buttons{3};
        CONNECT buttons{4} WITH B4;
        ISignalDI Knapp4, 1, buttons{4};
        TPErase;

        WHILE TRUE DO
        IF index > 1 THEN
        IF 2 * 4 = 4 THEN
        ENDIF
        TEST Remove()
        CASE 1 :
        DrawSquares targets{1}, sizes{1}, amounts{1};
        CASE 2 :
        DrawCircles targets{2}, sizes{2}, amounts{2};
        CASE 3 :
        DrawTriangles targets{3}, sizes{3}, amounts{3};
        CASE 4 : Configure;
        DEFAULT :
        ENDTEST
        ENDIF
        ENDWHILE

        BACKWARD
        ! dwadawd
        if 5 < 2 index := 2;
        TPWrite("Backward!");
        ERROR
        TPWrite("Error!");
        TPWrite("test");
        ENDPROC

        FUNC string name(PERS num name)

        ENDFUNC

        TRAP B1
        Set Lampa_knapp1;
        Store 1;
        Reset Lampa_knapp1;
        ENDTRAP

        TRAP B2
        Set Lampa_knapp2;
        Store 2;
        Reset Lampa_knapp2;
        ENDTRAP

        TRAP B3
        Set Lampa_knapp3;
        Store 3;
        Reset Lampa_knapp3;
        ENDTRAP

        TRAP B4
        Set Lampa_knapp4;
        Store 4;
        Reset Lampa_knapp4;
        ENDTRAP

        PROC Configure()
        VAR num choice;

        choice := askChoice("Section:", "Square", "Circle", "Triangle", "", "");
        IF choice > 0 AND choice < 4 OR Kamera ConfigureSection choice;
        ENDPROC

        PROC ConfigureSection(num section)
        amounts{section} := askValue("Quantity:");
        sizes{section} := askValue("Size:");
        targets{section} := Offs(middle, askValue("X-Offset:"), askValue("Y-Offset"), askValue("Z-Offset"));
        ENDPROC

        PROC Store(num value)
        queue{index} := value;
        index := index + 1;
        ENDPROC

        FUNC num Read()
        RETURN queue{1};
        ENDFUNC

        FUNC num Remove()
        VAR string message;

        VAR num value;

        value := queue{1};
        message := "Drawing:" + NumToStr(value, 0) + " Length:" + NumToStr(index - 2, 0) + " [";
        IF index > 1 THEN
        FOR i FROM 2 TO index DO
        queue{i - 1} := queue{i};
        IF(i < index) THEN
        IF(i > 2) message := message + ",";
        message := message + NumToStr(queue{i}, 0);
        ENDIF
        ENDFOR
        index := index - 1;
        ENDIF
        TPWrite message + "]";
        RETURN value;
        ENDFUNC

        PROC SafeGrab()
        IF Gripare_stäng = 0 AND Gripare_öppna = 1 Grab;
        ENDPROC

        PROC Grab()
        MoveJ [[-100.99, 35.23, 151.10], [0.00887397, 0.701348, 0.003933, -0.712753], [-1, -1, -1, 0], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]], v1000, z50, TCP_Penna\WObj := PennPlats;
        MoveJ [[-100.99, 35.20, 37.64], [0.00895862, 0.701356, 0.00392765, -0.712744], [-1, -1, -1, 0], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]], v200, z40, TCP_Penna\WObj := PennPlats;
        MoveL [[-99.94, 37.21, -15.90], [0.00114627, 0.707127, -0.00109817, -0.707085], [-1, -1, -1, 0], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]], v10, fine, TCP_Penna\WObj := PennPlats;
        Set Gripare_stäng;
        Reset Gripare_öppna;
        WaitTime 1;
        MoveL [[-100.57, 35.43, 34.30], [0.00820819, 0.700237, 0.00255569, -0.713858], [-1, -1, -1, 0], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]], v10, fine, TCP_Penna\WObj := PennPlats;
        MoveJ [[-100.60, 35.33, 149.96], [0.00853827, 0.700961, 0.00306344, -0.713142], [-1, -1, -1, 0], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]], v200, z40, TCP_Penna\WObj := PennPlats;
        ENDPROC

        PROC SafeRelease()
        IF(Gripare_stäng = 1 AND Gripare_öppna = 0) Release;
        ENDPROC

        PROC Release()
        MoveJ [[-100.99, 35.23, 151.10], [0.00887397, 0.701348, 0.003933, -0.712753], [-1, -1, -1, 0], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]], v1000, z50, TCP_Penna\WObj := PennPlats;
        MoveJ [[-100.99, 35.20, 37.64], [0.00895862, 0.701356, 0.00392765, -0.712744], [-1, -1, -1, 0], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]], v200, z40, TCP_Penna\WObj := PennPlats;
        MoveL [[-99.94, 37.21, -15.90], [0.00114627, 0.707127, -0.00109817, -0.707085], [-1, -1, -1, 0], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]], v10, fine, TCP_Penna\WObj := PennPlats;
        Reset Gripare_stäng;
        Set Gripare_öppna;
        WaitTime 1;
        MoveL [[-100.57, 35.43, 34.30], [0.00820819, 0.700237, 0.00255569, -0.713858], [-1, -1, -1, 0], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]], v10, fine, TCP_Penna\WObj := PennPlats;
        MoveJ [[-100.60, 35.33, 149.96], [0.00853827, 0.700961, 0.00306344, -0.713142], [-1, -1, -1, 0], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]], v200, z40, TCP_Penna\WObj := PennPlats;
        ENDPROC

        FUNC num askValue(string question)
        VAR num value;

        TPReadNum value, question;
        RETURN value;
        ENDFUNC

        FUNC num askChoice(string question, string choice1, string choice2, string choice3, string choice4, string choice5)
        VAR num value;

        TPReadFK value, question, choice1, choice2, choice3, choice4, choice5;
        RETURN value;
        ENDFUNC

        ENDMODULE

        MODULE StartModul

        CONST robtarget robtargetBas:=[[0, 0, 0], [0.002364117, 0.992700113, 0.055506584, -0.107050994], [-1, -1, 0, 1], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]];

        CONST robtarget PappersRam:=[[40, 40, -2], [0.002364117, 0.992700113, 0.055506584, -0.107050994], [-1, -1, 0, 1], [9E+09, 9E+09, 9E+09, 9E+09, 9E+09, 9E+09]];

        PROC RitaRamPåPappret ()
        MoveJ Offs (PappersRam, 0, 0, 150), v1000, z50, TCP_Penna\WObj:=Papper;
        MoveJ Offs (PappersRam, 0, 0, 20), v1000, z10, TCP_Penna\WObj:=Papper;
        MoveL Offs (PappersRam, 0, 0, 0), v200, fine, TCP_Penna\WObj:=Papper;
        MoveL Offs (PappersRam, 130, 0, 0), v200, fine, TCP_Penna\WObj:=Papper;
        MoveL Offs (PappersRam, 130, 217, 0), v200, fine, TCP_Penna\WObj:=Papper;
        MoveL Offs (PappersRam, 0, 217, 0), v200, fine, TCP_Penna\WObj:=Papper;
        MoveL Offs (PappersRam, 0, 0, 0), v200, fine, TCP_Penna\WObj:=Papper;
        MoveL Offs (PappersRam, 0, 0, 20), v500, z10, TCP_Penna\WObj:=Papper;
        MoveJ Offs (PappersRam, 0, 0, 150), v1000, z50, TCP_Penna\WObj:=Papper;
        ENDPROC

        ENDMODULE
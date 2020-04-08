/* Objective function */
min: +x0 +x1;

/* Constraints */
-x0 -y1 <= -4;
-x0 +y1 <= 4;
-x1 -y1 <= -8;
-x1 +y1 <= 8;

/* Variable bounds */
x0 <= 1e+20;
x1 <= 1e+20;
y0 <= 1e+20;
y1 <= 1e+20;

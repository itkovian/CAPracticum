assembly:
	gcc -o pell.s -O0 -c -S pell.c
pract02:
	gcc -o pell pell.s
pract02_debug:
	gcc -g -o pell_debug pell.s
pract02_opt:
	gcc -O1 -c -S -m32 -o pell_opt.s pell.c
	gcc -O1 -m32 pell.c -o pell_opt
clean:
	rm -rf pell pell_opt pell_debug

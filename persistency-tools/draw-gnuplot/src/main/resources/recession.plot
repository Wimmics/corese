reset

set terminal gif
#  set terminal pdf
set output "<OUTPUT_FILE>"

set title "<TITLE>" font "sans, 8"
set key left top
set xlabel 'Sample size [#edges]'
set logscale y 10
set logscale x 10
set ylabel 'Median request time'
set xtic auto
set ytic auto
set label "<GIT_VERSION>" at screen 0, screen 0

set style line 1 lc rgb '#0060ad' lt 1 lw 2 pt 7 ps 1.5   # --- blue
set style line 2 lc rgb '#dd181f' lt 1 lw 2 pt 7 ps 1.5   # --- red

plot '<DATA_FILENAME>' index 0 with linespoints ls 1 title '<TITLE_SET1>', \
     ''                index 1 with linespoints ls 2 title '<TITLE_SET2>'

set terminal svg
set output "/tmp/test.svg"
replot

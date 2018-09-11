SystemDescription.pdf: SystemDescription.tex
	pdflatex SystemDescription.tex
	bibtex SystemDescription.aux
	pdflatex SystemDescription.tex
	pdflatex SystemDescription.tex


.PHONY: clean

clean:
	rm -f *.log *.out

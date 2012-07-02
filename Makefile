all: ponyoftheday

ponyoftheday:
	javac7 -source 7 -target 7 -cp . -s src -d . src/se/kth/maandree/ponyoftheday/Program.java || javac -source 7 -target 7 -cp . -s src -d . src/se/kth/maandree/ponyoftheday/Program.java
	jar7 -cfm pony-of-the-day.jar META-INF/MANIFEST.MF se/kth/maandree/ponyoftheday/Program.class || jar -cfm pony-of-the-day.jar META-INF/MANIFEST.MF se/kth/maandree/ponyoftheday/Program.class

install: all
	install -d "${DESTDIR}/usr/bin"
	install -d "${DESTDIR}/usr/share"
	install -m 755 pony-of-the-day{,.jar} "${DESTDIR}/usr/bin"
	install -m 644 commands "${DESTDIR}/usr/share/pony-of-the-day"

uninstall:
	unlink "${DESTDIR}/usr/bin/pony-of-the-day"
	unlink "${DESTDIR}/usr/bin/pony-of-the-day.jar"
	unlink "${DESTDIR}/usr/share/pony-of-the-day"

clean:
	rm -r se
	rm pony-of-the-day.jar

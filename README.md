Server that uses the Quote of the Day protocol,
and can send my little ponies.

By default it will sends a quote from fortune-mod,
but if UDP is used the following is done:

input: "pony",   output: displays a pony
input: "cow",    output: displays a cow
input: "qoute",  output: displays a pony qoute
input: "pony+",  output: displays a pony with a fortune-mod qoute
input: "cow+",   output: displays a cow with a fortune-mod qoute
input: "qoute+", output: displays a pony qoute with the pony

Prepending tty to the input will make the displayed ponies
suitable for Linux VT (TTY), rather than 256 colour terminals.

Output is in UTF-8, and in case of ponies, with ANSI escape seqences.

The intervals between use is, by default, restricted to 12 hours.

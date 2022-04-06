Programowanie klient serwer z użyciem gniazd, kanałów i selektorów


Aplikacja obsługuje rozsyłanie wiadomości do klientów.

Każdy klient, łącząc się z serwerem, może zapisać się do świadczonej usługi podając tematy, którymi się interesuje (np. polityka, sport, celebryci, gotowanie, randki, ...) jak również zrezygnować ze swoich istniejących tematów. Aplikacja powinna umożliwić jednocześną obsługę wielu klientów.

Osobnym programem (administrującym tematami/wiadomościami) przesyłamy do serwera wiadomości z różnych dziedzin, a on rozsyła je do subskrybentów zainteresowanych danym tematem. Tym samym programem, przy pomocy serwera, zarządzamy tematami (usuwanie istniejących tematów, dodawanie nowych tematów, informowanie klientom o zmianach dotyczących tematów, ...).

Do obsługi połączeń (typu "subscribe", "unsubscribe", oraz połączeń przysyłających nowe wiadomości do rozesłania) użyj selektorów - nie twórz nowych wątków!

Stwórz proste GUI separujące od logiki przetwarzania danych.

Aplikacja powinna być odporna na różne sytuacje awaryjne.

# 📱 Zeon – Pametni Fitness Asistent & Sustav za Povezivanje Trenera i Klijenta

Zeon je napredna Android mobilna aplikacija namijenjena praćenju fitness ciljeva, zdravog načina života i fizičkog napretka korisnika. Za razliku od klasičnih aplikacija koje su fokusirane isključivo na individualno praćenje, Zeon donosi integraciju svih ključnih aspekata fitness procesa – od unosa hrane, kalorija i vode do naprednog nadzora treninga. Ključna prednost aplikacije je izravna povezanost između klijenta i trenera u stvarnom vremenu.

> 👥 **Napomena o projektu:** Ovaj projekt je razvijen u sklopu timskog rada na fakultetu (4 člana). Repozitorij sadrži očišćenu strukturu s fokusom na izvorni kod (Source Code), gdje je moj primarni doprinos obuhvaćao kompletan razvoj **modula za prehranu i kalorijski unos**, implementaciju **lokalne baze podataka (Room/SQLite)** te integraciju s **eksternim API-jem za dohvat recepata**.

---

## 📐 Arhitektura sustava (Multi-layered Architecture)

Aplikacija je građena strogo prema principima višeslojne arhitekture (3-tier architecture) kako bi se osigurala modularnost, lakše testiranje i čisti kod:

1. **Prezentacijski sloj (UI):** Odgovoran za korisničko sučelje, prikaz podataka, osnovnu validaciju korisničkih unosa te pozivanje poslovne logike.
2. **Sloj poslovne logike:** Implementira poslovna pravila i kompleksne operacije (izračuni napretka, statistika, kalorijski ciljevi). Organiziran je kroz model klase i servisne klase uz podršku za pozadinske servise.
3. **Sloj pristupa podacima (Data Access):** Izoliran putem repozitorijskih klasa (*Repository Pattern*), zadužen za komunikaciju s lokalnom bazom podataka i udaljenim REST API servisima.

---

## 🛠️ Tehnološki stog (Tech Stack)

- **Jezik i platforma:** Kotlin (Android SDK)
- **Lokalna pohrana podataka:** Room Persistence Library (SQLite baza podataka)
- **Asinkrono programiranje:** Kotlin Coroutines & Flow (za reaktivno upravljanje podacima i pozadinske procese)
- **Mrežna komunikacija:** REST API integracija za dohvat recepata i simulaciju poruka
- **Alati:** Git & GitHub (verzije koda), GitHub Projects (Agile/Scrum planiranje)

---

## 🚀 Funkcionalnosti i Moj Doprinos

Sustav je podijeljen na module, a u tablici ispod istaknute su funkcionalnosti s naglaskom na module koje sam samostalno dizajnirao i programirao:

| Oznaka | Naziv | Kratki opis | Razvijatelj |
| :--- | :--- | :--- | :--- |
| **F02** | **Upravljanje prehranom** | *Pretraživanje i odabir namirnica iz baze, evidentiranje unosa hrane/pića te mogućnost dodavanja novih namirnica s nutritivnim vrijednostima u lokalnu Room bazu.* | **Dorian Šturbek** |
| **F05** | **Praćenje kalorijskog unosa** | *Automatski izračun ukupnog dnevnog unosa kalorija i makronutrijenata (proteini, masti, ugljikohidrati) te usporedba s definiranim ciljem.* | **Dorian Šturbek** |
| **F12** | **Pregledavanje recepata** | *Dinamičko pretraživanje i pregledavanje zdravih recepata sukladno korisnikovim potrebama, uz asinkroni dohvat podataka s eksternog API-ja.* | **Dorian Šturbek** |
| F01 | Login/Registracija | Autentifikacija i registracija korisnika s osobnim podacima. | Timski rad |
| F03 | Pronalazak trenera | Pregled, filtriranje i slanje zahtjeva za povezivanje s fitness trenerom. | Timski rad |
| F04 | Kalendar aktivnosti | Pregled kalendara s planiranim treninzima dodijeljenim od strane trenera. | Timski rad |
| F06 | Unos podataka o vježbanju | Unos odrađenih vježbi, serija, ponavljanja, težina i komentara na trening. | Timski rad |
| F07 | Primanje obavijesti | Prikaz obavijesti o nadolazećim treninzima u realnom vremenu. | Timski rad |
| F08 | Upravljanje profilom | Unos i antropometrijsko praćenje (promjene visine/težine kroz vrijeme). | Timski rad |
| F09 | Pregled pojedine vježbe | Detaljan pregled opisa vježbe, mišićne skupine i video demonstracije. | Timski rad |
| F10 | Slanje poruka treneru | Dvosmjerna komunikacija s trenerom i dohvaćanje novih poruka preko API-ja. | Timski rad |
| F11 | Praćenje statistike | Pregled povijesti dostignuća, osobnih rekorda (PR) i analitika napretka. | Timski rad |

---

## 🔒 Ključne tehničke značajke mog koda

- **Room & SQLite Optimizacija:** Za potrebe modula prehrane dizajnirao sam relacijsku strukturu tablica koja omogućuje brzo lokalno pretraživanje hrane bez usporavanja aplikacije.
- **Reaktivno praćenje (Flow & Coroutines):** Izračun kalorija (F05) se ažurira u stvarnom vremenu na korisničkom sučelju čim korisnik doda novu namirnicu, koristeći asinkrone Kotlin Coroutinese koji ne blokiraju glavnu (UI) nit.
- **Siguran API dohvat:** Integrirao sam eksterni REST API za recepte uz pravilno hendlanje mrežnih pogrešaka (error handling) i keširanje osnovnih podataka.

---

## 📁 Struktura repozitorija (Pregled izvornog koda)

```text
├── com/
│   └── zeon/
│       ├── data/          # Sloj podataka: Room baze, DAO sučelja, API servisi, Repozitoriji
│       ├── domain/        # Poslovna logika: Servisi, klase modela i poslovna pravila
│       └── ui/            # Prezentacijski sloj: Activity, Fragmenti, ViewModels, Adapteri
└── layouts/               # XML datoteke korisničkog sučelja (UI dizajni ekrana)

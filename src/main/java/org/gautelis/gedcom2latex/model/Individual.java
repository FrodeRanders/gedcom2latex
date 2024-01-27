package org.gautelis.gedcom2latex.model;

import org.gautelis.gedcom2latex.LineHandler;
import org.gautelis.gedcom2latex.model.gedcom.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Individual {
    private static final Logger log = LoggerFactory.getLogger(Individual.class);

    private final INDI self;
    private Individual father;
    private Individual mother;
    private final Collection<Individual> children = new ArrayList<>();
    private final Map</* id */ String, Individual> spouses = new HashMap<>();

    public Individual(final INDI self) {
        Objects.requireNonNull(self, "self");
        this.self = self;
    }

    public String getId() {
        return self.getId();
    }

    public SEX getSex() {
        return self.getSex();
    }

    public Collection<Name> getNames() {
        Collection<Name> names = new ArrayList<>();
        for (NAME name : self.NAME()) {
            String annotatedName = name.getName().replace("?", "").replace("\\", "");
            Optional<String> givenName = name.getGivenName();
            Optional<String> surname = name.getSurname();

            names.add(new Name(annotatedName, givenName.orElse(""), surname.orElse("")));
        }
        return names;
    }

    public void setFather(String familyId, Individual father) {
        Objects.requireNonNull(father, "father");

        this.father = father;
        if (null != mother) {
            father.addSpouse(familyId, mother);
        }
    }

    public Optional<Individual> getFather() {
        return Optional.ofNullable(father);
    }

    public void setMother(String familyId, Individual mother) {
        Objects.requireNonNull(mother, "mother");

        this.mother = mother;
        if (null != father) {
            mother.addSpouse(familyId, father);
        }
    }

    public Optional<Individual> getMother() {
        return Optional.ofNullable(mother);
    }

    public void addSpouse(String familyId, Individual spouse) {
        Objects.requireNonNull(spouse, "spouse");

        if (!spouses.containsKey(familyId)) {
            spouses.put(familyId, spouse);
        }

        if (!spouse.spouses.containsKey(familyId)) {
            spouse.spouses.put(familyId, this);
        }
    }

    public Collection<Individual> getSpouses() {
        return spouses.values();
    }

    public void addChild(Individual child) {
        Objects.requireNonNull(child, "child");
        children.add(child);
    }

    public Collection<Individual> getChildren() {
        return children;
    }

    public Collection<DatePlace> getBirths() {
        Collection<DatePlace> births = new ArrayList<>();
        for (BIRT birth : self.BIRT()) {
            Optional<String> date = birth.getDate();
            Optional<String> place = birth.getPlace();

            if (date.isPresent() || place.isPresent()) {
                births.add(new DatePlace(date.orElse(""), place.orElse("")));
            }
        }
        return births;
    }

    public Collection<DatePlace> getBaptisms() {
        Collection<DatePlace> baptisms = new ArrayList<>();
        for (CHR baptism : self.CHR()) {
            Optional<String> date = baptism.getDate();
            Optional<String> place = baptism.getPlace();

            if (date.isPresent() || place.isPresent()) {
                baptisms.add(new DatePlace(date.orElse(""), place.orElse("")));
            }
        }
        return baptisms;
    }

    public Collection<DatePlace> getDeaths() {
        Collection<DatePlace> deaths = new ArrayList<>();
        for (DEAT death : self.DEAT()) {
            Optional<String> date = death.getDate();
            Optional<String> place = death.getPlace();

            if (date.isPresent() || place.isPresent()) {
                deaths.add(new DatePlace(date.orElse(""), place.orElse("")));
            }
        }
        return deaths;
    }

    public Collection<DatePlace> getBurials() {
        Collection<DatePlace> burials = new ArrayList<>();
        for (BURI burial : self.BURI()) {
            Optional<String> date = burial.getDate();
            Optional<String> place = burial.getPlace();

            if (date.isPresent() || place.isPresent()) {
                burials.add(new DatePlace(date.orElse(""), place.orElse("")));
            }
        }
        return burials;
    }

    public Collection<URI> getURIs() {
        Collection<URI> uris = new ArrayList<>();
        for (OBJE record : self.OBJE()) {
            Collection<FILE> files = record.FILE();
            for (FILE file : files) {
                String reference = file.getReference();
                if (null != reference && (reference.startsWith("http:") || reference.startsWith("https:"))) {
                    try {
                        URI uri = new URI(reference);
                        uris.add(uri);
                    } catch (URISyntaxException urise) {
                        String info = "Cant treat reference: \"" + reference + "\": ";
                        info += urise.getMessage();
                        log.info(info, urise);
                    }
                }
            }
        }
        return uris;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("[Individual");
        buf.append(" self=").append(self);
        if (null != father) {
            buf.append(" father=").append(father);
        }
        if (null != mother) {
            buf.append(" mother=" + mother);
        }
        for (Individual child : children) {
            buf.append(" child=").append(child);
        }
        buf.append("]");
        return buf.toString();
    }

public String asProband() {
    StringBuilder buf = new StringBuilder("g[");
    buf.append("id=").append(getId());
    buf.append(",").append(self.getSex().name().toLowerCase()); // lower case because of genealogytree package
    buf.append("]").append("{");

    String displayedName = "";
    for (Name name : getNames()) {
        String givenName = name.givenName();
        String surname = name.surname();

        if (!givenName.isEmpty() || !surname.isEmpty()) {
            displayedName = givenName.replace("\"", "'");
            displayedName += " ";
            displayedName += "\\surn{";
            displayedName += surname.replace("\"", "'");
            displayedName += "}";
            break;
        } else {
            displayedName = name.annotatedName().replace("/", "");
        }
    }
    buf.append(displayedName);

    if (!displayedName.isEmpty()) {
        for (DatePlace birth : getBirths()) {
            String date = birth.date();
            if (!date.isEmpty()) {
                buf.append("\\\\\\gtrsymBorn\\,").append(birth.date().toLowerCase());
            }
            break;
        }
        for (DatePlace death : getDeaths()) {
            String date = death.date();
            if (!date.isEmpty()) {
                buf.append("\\\\\\gtrsymDied\\,").append(death.date().toLowerCase());
            }
            break;
        }
    }

    //buf.append(" \\ref{sec:").append(getId()).append("}");
    buf.append("}");
    return buf.toString();
}


    public String asCoreRelationship() {
        StringBuilder buf = new StringBuilder("{parent");
        Optional<FAMC> famc = self.FAMC().stream().findFirst(); // expects only one child to family link
        famc.ifPresent(value -> buf.append("[id=").append(value.getFamilyId()).append("]"));
        buf.append("{");
        buf.append(asProband());

        if (null != father) {
            buf.append("parent {");
            buf.append(father.asProband());
            buf.append("}");
        }

        if (null != mother) {
            buf.append("parent {");
            buf.append(mother.asProband());
            buf.append("}");
        }
        buf.append("}}");
        return buf.toString();
    }

    public static String produceLatexOutput() {
        /*
 sandclock{
  child[id=GauaOsth1805]{
    p[id=OsthJoha1780]{
      female,
      name={\pref{Johanna} Elisabeth Rosina \surn{Osthoff}},
      birth={1780-05-08}{Braunschweig (Niedersachsen)},
      marriage={1805-10-09}{Braunschweig (Niedersachsen)},
      death={1809-10-11}{G\"ottingen (Niedersachsen)},
      comment={Wei\ss{}gerberstochter},
    }
    g[id=GauxCarl1777]{
      male,
      name={Johann \pref{Carl Friedrich} \surn{Gau\ss{}}},
      birth={1777-04-30}{Braunschweig (Niedersachsen)},
      death={1855-02-23}{G\"ottingen (Niedersachsen)},
      profession={Mathematiker, Astronom, Geod\"at und Physiker},
    }
    c[id=GauxCarl1806]{
      male,
      name={\pref{Carl} Joseph \surn{Gau\ss{}}},
      birth={1806-08-21}{Braunschweig (Niedersachsen)},
      death={1873-07-04}{Hannover (Niedersachsen)},
    }
    c[id=GauxWilh1808]{
      female,
      name={\pref{Wilhelmina} \surn{Gau\ss{}}},
      birth={1808-02-29}{G\"ottingen (Niedersachsen)},
      death={1840-08-12}{T\"ubingen (Baden-W\"urttemberg)},
    }
    c[id=GauxLudw1809]{
      male,
      name={\pref{Ludwig} \surn{Gau\ss{}}},
      birth={1809-09-10}{G\"ottingen (Niedersachsen)},
      death={1810-03-01}{G\"ottingen (Niedersachsen)},
    }
    union[id=GauaWald1810]{
      p[id=WaldFrie1788]{
        female,
        name={\pref{Friederica} Wilhelmine \surn{Waldeck}},
        birth={1788-04-15}{G\"ottingen (Niedersachsen)},
        marriage={1810-08-14}{G\"ottingen (Niedersachsen)},
        death={1831-09-12}{G\"ottingen (Niedersachsen)},
        comment={Rechtswissenschaftlerstochter},
      }
      c[id=GauxEuge1811]{
        male,
        name={\pref{Eugen} Peter Samuel Marius \surn{Gau\ss{}}},
        birth={1811-07-29}{G\"ottingen (Niedersachsen)},
        death={1896-07-04}{Columbia (Missouri)},
        profession={Rechtswissenschaftler, Kaufmann},
      }
      c[id=GauxWilh1813]{
        male,
        name={\pref{Wilhelm} August Carl Matthias \surn{Gau\ss{}}},
        birth={1813-10-23}{G\"ottingen (Niedersachsen)},
        death={1879-08-23}{St. Louis (Missouri)},
      }
      c[id=GauxTher1816]{
        female,
        name={Henriette Wilhelmine Karoline \pref{Therese} \surn{Gau\ss{}}},
        birth={1816-06-09}{G\"ottingen (Niedersachsen)},
        death={1864-02-11}{Dresden (Sachsen)},
      }
    }
  }
  parent[id=GoosEgge1735]{
    g[id=GauxGebh1743]{
      male,
      name={\pref{Gebhard} Dietrich \surn{Gau\ss{}}},
      birth={1743-02-13}{Braunschweig (Niedersachsen)},
      death={1808-04-14}{Braunschweig (Niedersachsen)},
      profession={G\"artner, Wasserkunstmeister, Rechnungsf\"uhrer},
    }
    parent[id=GoosLbtk1705]{
      g[id=GoosJyrg1715]{
        male,
        name={\pref{J\"urgen} \surn{Gooss}},
        birth={1715}{V\"olkenrode (Niedersachen)},
        death={1774-07-05}{Braunschweig (Niedersachsen)},
        profession={Lehmmaurer},
      }
      p[id=GoosHinr1655]{
        male,
        name={\pref{Hinrich} \surn{Gooss}},
        birth={(caAD)1655}{},
        death={1726-10-25}{V\"olkenrode (Niedersachen)},
      }
      p[id=LxtkKath1674]{
        female,
        name={\pref{Katharina} \surn{L\"utken}},
        birth={1674-08-19}{V\"olkenrode (Niedersachen)},
        marriage={1705-11-24}{V\"olkenrode (Niedersachen)},
        death={1749-04-15}{V\"olkenrode (Niedersachen)},
      }
    }
    p[id=EggeKath1710]{
      female,
      name={\pref{Katharina} Magdalena \surn{Eggenlings}},
      birth={(caAD)1710}{Rethen},
      marriage={(caAD)1735}{V\"olkenrode (Niedersachen)},
      death={1774-04-03}{Braunschweig (Niedersachsen)},
    }
  }
  parent[id=BentKron1740]{
    g[id=BenzDoro1743]{
      female,
      name={\pref{Dorothea} \surn{Benze}},
      birth={1743-06-18}{Velpke (Niedersachsen)},
      marriage={1776-04-25}{Velpke (Niedersachsen)},
      death={1839-04-18}{G\"ottingen (Niedersachsen)},
      comment={Steinhauerstochter},
    }
    parent[id=BentBbbb1740]{
      g[id=BentChri1717]{
        male,
        name={\pref{Christoph} \surn{Bentze}},
        birth={1717}{Velpke (Niedersachsen)},
        death={1748-09-01}{Velpke (Niedersachsen)},
        profession={Steinhauer},
      }
      p[id=BentAndr1687]{
        male,
        name={\pref{Andreas} \surn{Bentze}},
        birth={1687-02}{},
        death={(caAD)1750}{Velpke (Niedersachsen)},
      }
    }
    p[id=KronKath1710]{
      female,
      name={\pref{Katharina} \surn{Krone}},
      birth={(caAD)1710}{},
      death={1743/}{Velpke (Niedersachsen)},
    }
  }
}

         */
        StringBuilder buf = new StringBuilder();

        if (true) {

            buf.append("{ parent{");
            buf.append("g[female]{first child}");
            buf.append("c[male]{second child}");
            buf.append("c[female]{third child}");
            buf.append("parent{");
            buf.append("c[female]{aunt}");
            buf.append("g[male]{father}");
            buf.append("c[male]{uncle}");
            buf.append("parent");
            buf.append("{");
            buf.append("g[male]{grandfather}");
            buf.append("p[male]{great-grandfather}");
            buf.append("p[female]{great-grandmother}");
            buf.append("} parent{");
            buf.append("g[female]{grandmother}");
            buf.append("p[male]{great-grandfather 2}");
            buf.append("p[female]{great-grandmother 2}");
            buf.append("c[male]{granduncle}");
            buf.append("} }");
            buf.append("parent");
            buf.append("{");
            buf.append("c[male]{uncle 2}");
            buf.append("g[female]{mother}");
            buf.append("p[male]{grandfather 2}");
            buf.append("p[female]{grandmother 2}");
            buf.append("}");
            buf.append("}");
            buf.append("}");

        } else {
            buf.append("sandclock{");
            buf.append("  child[id=GauaOsth1805]{");
            buf.append("    p[id=OsthJoha1780]{");
            buf.append("      female,");
            buf.append("      name={\\pref{Johanna} Elisabeth Rosina \\surn{Osthoff}},");
            buf.append("      birth={1780-05-08}{Braunschweig (Niedersachsen)},");
            buf.append("      marriage={1805-10-09}{Braunschweig (Niedersachsen)},");
            buf.append("      death={1809-10-11}{Gottingen (Niedersachsen)},");
            buf.append("      comment={Wei\\ss{}gerberstochter},");
            buf.append("    }");
            buf.append("    g[id=GauxCarl1777]{");
            buf.append("      male,");
            buf.append("      name={Johann \\pref{Carl Friedrich} \\surn{Gau\\ss{}}},");
            buf.append("      birth={1777-04-30}{Braunschweig (Niedersachsen)},");
            buf.append("      death={1855-02-23}{Gottingen (Niedersachsen)},");
            buf.append("      profession={Mathematiker, Astronom, Geodat und Physiker},");
            buf.append("    }");
            buf.append("    c[id=GauxCarl1806]{");
            buf.append("      male,");
            buf.append("      name={\\pref{Carl} Joseph \\surn{Gau\\ss{}}},");
            buf.append("      birth={1806-08-21}{Braunschweig (Niedersachsen)},");
            buf.append("      death={1873-07-04}{Hannover (Niedersachsen)},");
            buf.append("    }");
            buf.append("    c[id=GauxWilh1808]{");
            buf.append("      female,");
            buf.append("      name={\\pref{Wilhelmina} \\surn{Gau\\ss{}}},");
            buf.append("      birth={1808-02-29}{Gottingen (Niedersachsen)},");
            buf.append("      death={1840-08-12}{Tubingen (Baden-Wurttemberg)},");
            buf.append("    }");
            buf.append("    c[id=GauxLudw1809]{");
            buf.append("      male,");
            buf.append("      name={\\pref{Ludwig} \\surn{Gau\\ss{}}},");
            buf.append("      birth={1809-09-10}{Gottingen (Niedersachsen)},");
            buf.append("      death={1810-03-01}{Gottingen (Niedersachsen)},");
            buf.append("    }");
            buf.append("    union[id=GauaWald1810]{");
            buf.append("      p[id=WaldFrie1788]{");
            buf.append("        female,");
            buf.append("        name={\\pref{Friederica} Wilhelmine \\surn{Waldeck}},");
            buf.append("        birth={1788-04-15}{Gottingen (Niedersachsen)},");
            buf.append("        marriage={1810-08-14}{Gottingen (Niedersachsen)},");
            buf.append("        death={1831-09-12}{Gottingen (Niedersachsen)},");
            buf.append("        comment={Rechtswissenschaftlerstochter},");
            buf.append("      }");
            buf.append("      c[id=GauxEuge1811]{");
            buf.append("        male,");
            buf.append("        name={\\pref{Eugen} Peter Samuel Marius \\surn{Gau\\ss{}}},");
            buf.append("        birth={1811-07-29}{Gottingen (Niedersachsen)},");
            buf.append("        death={1896-07-04}{Columbia (Missouri)},");
            buf.append("        profession={Rechtswissenschaftler, Kaufmann},");
            buf.append("      }");
            buf.append("      c[id=GauxWilh1813]{");
            buf.append("        male,");
            buf.append("        name={\\pref{Wilhelm} August Carl Matthias \\surn{Gau\\ss{}}},");
            buf.append("        birth={1813-10-23}{Gottingen (Niedersachsen)},");
            buf.append("        death={1879-08-23}{St. Louis (Missouri)},");
            buf.append("      }");
            buf.append("      c[id=GauxTher1816]{");
            buf.append("        female,");
            buf.append("        name={Henriette Wilhelmine Karoline \\pref{Therese} \\surn{Gau\\ss{}}},");
            buf.append("        birth={1816-06-09}{Gottingen (Niedersachsen)},");
            buf.append("        death={1864-02-11}{Dresden (Sachsen)},");
            buf.append("      }");
            buf.append("    }");
            buf.append("  }");
            buf.append("  parent[id=GoosEgge1735]{");
            buf.append("    g[id=GauxGebh1743]{");
            buf.append("      male,");
            buf.append("      name={\\pref{Gebhard} Dietrich \\surn{Gau\\ss{}}},");
            buf.append("      birth={1743-02-13}{Braunschweig (Niedersachsen)},");
            buf.append("      death={1808-04-14}{Braunschweig (Niedersachsen)},");
            buf.append("      profession={Gartner, Wasserkunstmeister, Rechnungsfuhrer},");
            buf.append("    }");
            buf.append("    parent[id=GoosLbtk1705]{");
            buf.append("      g[id=GoosJyrg1715]{");
            buf.append("        male,");
            buf.append("        name={\\pref{Jurgen} \\surn{Gooss}},");
            buf.append("        birth={1715}{Volkenrode (Niedersachen)},");
            buf.append("        death={1774-07-05}{Braunschweig (Niedersachsen)},");
            buf.append("        profession={Lehmmaurer},");
            buf.append("      }");
            buf.append("      p[id=GoosHinr1655]{");
            buf.append("        male,");
            buf.append("        name={\\pref{Hinrich} \\surn{Gooss}},");
            buf.append("        birth={(caAD)1655}{},");
            buf.append("        death={1726-10-25}{Volkenrode (Niedersachen)},");
            buf.append("      }");
            buf.append("      p[id=LxtkKath1674]{");
            buf.append("        female,");
            buf.append("        name={\\pref{Katharina} \\surn{Lutken}},");
            buf.append("        birth={1674-08-19}{Volkenrode (Niedersachen)},");
            buf.append("        marriage={1705-11-24}{Volkenrode (Niedersachen)},");
            buf.append("        death={1749-04-15}{Volkenrode (Niedersachen)},");
            buf.append("      }");
            buf.append("    }");
            buf.append("    p[id=EggeKath1710]{");
            buf.append("      female,");
            buf.append("      name={\\pref{Katharina} Magdalena \\surn{Eggenlings}},");
            buf.append("      birth={(caAD)1710}{Rethen},");
            buf.append("      marriage={(caAD)1735}{Volkenrode (Niedersachen)},");
            buf.append("      death={1774-04-03}{Braunschweig (Niedersachsen)},");
            buf.append("    }");
            buf.append("  }");
            buf.append("  parent[id=BentKron1740]{");
            buf.append("    g[id=BenzDoro1743]{");
            buf.append("      female,");
            buf.append("      name={\\pref{Dorothea} \\surn{Benze}},");
            buf.append("      birth={1743-06-18}{Velpke (Niedersachsen)},");
            buf.append("      marriage={1776-04-25}{Velpke (Niedersachsen)},");
            buf.append("      death={1839-04-18}{Gottingen (Niedersachsen)},");
            buf.append("      comment={Steinhauerstochter},");
            buf.append("    }");
            buf.append("    parent[id=BentBbbb1740]{");
            buf.append("      g[id=BentChri1717]{");
            buf.append("        male,");
            buf.append("        name={\\pref{Christoph} \\surn{Bentze}},");
            buf.append("        birth={1717}{Velpke (Niedersachsen)},");
            buf.append("        death={1748-09-01}{Velpke (Niedersachsen)},");
            buf.append("        profession={Steinhauer},");
            buf.append("      }");
            buf.append("      p[id=BentAndr1687]{");
            buf.append("        male,");
            buf.append("        name={\\pref{Andreas} \\surn{Bentze}},");
            buf.append("        birth={1687-02}{},");
            buf.append("        death={(caAD)1750}{Velpke (Niedersachsen)},");
            buf.append("      }");
            buf.append("    }");
            buf.append("    p[id=KronKath1710]{");
            buf.append("      female,");
            buf.append("      name={\\pref{Katharina} \\surn{Krone}},");
            buf.append("      birth={(caAD)1710}{},");
            buf.append("      death={1743/}{Velpke (Niedersachsen)},");
            buf.append("    }");
            buf.append("  }");
            buf.append("}");
        }
        return buf.toString();
    }
}

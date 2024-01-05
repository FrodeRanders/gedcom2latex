package org.gautelis.gedcom2latex.model;

import org.gautelis.gedcom2latex.model.gedcom.INDI;
import org.gautelis.gedcom2latex.model.gedcom.NAME;

import java.util.*;

public class Individual {
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

    public Collection<String> getNames() {
        Collection<String> names = new ArrayList<>();
        for (NAME name : self.NAME()) {
            names.add(name.getName());
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
}

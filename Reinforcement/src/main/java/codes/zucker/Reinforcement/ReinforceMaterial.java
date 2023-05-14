package codes.zucker.Reinforcement;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public class ReinforceMaterial {
    public static List<ReinforceMaterial> Entries = new ArrayList<>();

    private Material material;
    private int breaksPerReinforce;
    private int maxAllowedBreaks;

    public ReinforceMaterial(Material material, int breaksPerReinforce, int maxAllowedBreaks) {
        this.material = material;
        this.breaksPerReinforce = breaksPerReinforce;
        this.maxAllowedBreaks = maxAllowedBreaks;
    }

    public Material getMaterial() {
        return material;
    }

    public int getBreaksPerReinforce() {
        return breaksPerReinforce;
    }

    public int getMaxAllowedBreaks() {
        return maxAllowedBreaks;
    }

    public static ReinforceMaterial GetFromMaterial(Material material) {
        for(ReinforceMaterial reinforceMaterial : Entries) {
            if (reinforceMaterial.material.name().equals(material.name()))
                return reinforceMaterial;
        }
        return null;
    }

    public static ReinforceMaterial GetFromMaterial(String material) {
        Material m = Material.getMaterial(material);
        return GetFromMaterial(m);
    }
}

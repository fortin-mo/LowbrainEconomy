package lowbrain.economy.main;

import lowbrain.library.fn;
import org.bukkit.Material;

public class Helper {
    public static String getItemFullName(String item) {
        String name = "";

        if (fn.isInt(item)) {
            int id = Integer.parseInt(item);
            Material mat = Material.getMaterial(id);
            if (mat != null)
                name = mat.name();
        } else if (item.indexOf(":") > 0) {
            String[] s = item.split(":");
            if (fn.isInt(s[0])) {
                int id = Integer.parseInt(s[0]);
                Material mat = Material.getMaterial(id);
                if (mat != null) {
                    name = mat.name();
                    for (int i = 1; i < s.length; i++)
                        name += !s[i].isEmpty() ? (":" + s[i]) : "";
                }
            }
        } else {
            name = item;
        }

        return name;
    }
}

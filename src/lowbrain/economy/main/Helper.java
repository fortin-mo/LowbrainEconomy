package lowbrain.economy.main;

import lowbrain.library.fn;
import org.bukkit.Material;

public class Helper {
    public static String getItemFullName(String item) {
        String name = "";

        String _id = "0";
        String[] s = item.split(":");

        if (s.length > 2)
            return item; // if there are more than one :, return as full string

        name = s[0];

        if (s.length == 2)
            _id = s[1];

        if (fn.isInt(name)) {
            Material mat = Material.getMaterial(Integer.parseInt(name));
            if (mat != null)
                name = mat.name();
        }

        if (!_id.equals("0")) // if _id is 0, no need for it, e.g INK_SACK = INK_SACK:0
            name += ":" + _id; // recombine name and id, e.g INK_SACK:15 or from 315:15

        return name;
    }
}

package com.maxMustermannGeheim.linkcollection.Daten;

import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public interface ParentClass_Alias {
//    static String getName(ParentClass parentClass) {
//        try {
//            return (String) parentClass.getClass().getDeclaredMethod("getName").invoke(parentClass);
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
////            e.printStackTrace();
//            return null;
//        }
//    }


    static String getNameAlias(ParentClass parentClass) {
        if (parentClass instanceof ParentClass_Alias) {
            try {

                return (String) parentClass.getClass().getDeclaredMethod("getNameAliases").invoke(parentClass);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                return parentClass.getName();
            }
        } else
            return parentClass.getName();
    }

    static ParentClass applyNameAndAlias(ParentClass parentClass, String text) {
        if (!(parentClass instanceof ParentClass_Alias) || !text.contains("\n")) {
            parentClass.setName(text);
            try {
                parentClass.getClass().getMethod("setNameAliases", String.class).invoke(parentClass, (Object) null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
        } else {
            String[] split = text.split("\\n");
            parentClass.setName(split[0]);
            try {
                parentClass.getClass().getMethod("setNameAliases", String.class).invoke(parentClass, (Object) String.join("\n", Arrays.copyOfRange(split, 1, split.length)));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return parentClass;
    }

    static String combineNameAndAlias(ParentClass parentClass) {
        if (parentClass instanceof ParentClass_Alias) {
            try {
                return parentClass.getName() + Utility.isNullReturnOrElse((String) parentClass.getClass().getMethod("getNameAliases").invoke(parentClass), "", s -> "\n" + s);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return parentClass.getName();

    }

    static boolean addAlias(ParentClass parentClass, String alias) {
        if (parentClass instanceof ParentClass_Alias) {
            try {
                String oldAlias = (String) parentClass.getClass().getDeclaredMethod("getNameAliases").invoke(parentClass);
                String newAlias;
                if (CustomUtility.stringExists(oldAlias))
                    newAlias = oldAlias + "\n" + alias;
                else
                    newAlias = alias;
                parentClass.getClass().getDeclaredMethod("setNameAliases", String.class).invoke(parentClass, newAlias);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                return false;
            }
        }
        return true;
    }

    // ---------------

    static boolean containsQuery(ParentClass parentClass, String query) {
        if (parentClass == null || query == null)
            return false;
        query = query.toLowerCase().replaceAll("[-_ ]", "");
        String name = parentClass.getName().toLowerCase().replaceAll("[-_ ]", "");
        if (name.contains(query))
            return true;
        else if (parentClass instanceof ParentClass_Alias) {
            try {
                String finalQuery = query;
                return Utility.isNullReturnOrElse((String) parentClass.getClass().getMethod("getNameAliases").invoke(parentClass), false, s -> s.toLowerCase().replaceAll("[-_ ]", "").contains(finalQuery));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return false;
            }
        } else
            return false;
    }

    static boolean containedInQuery(ParentClass parentClass, String query) {
        query = query.toLowerCase().replaceAll("[-_ ]", "");
        String name = parentClass.getName().toLowerCase().replaceAll("[-_ ]", "");
        if (query.contains(name))
            return true;
        else if (parentClass instanceof ParentClass_Alias) {
            try {
                String finalQuery = query;
                return (Utility.isNullReturnOrElse((String) parentClass.getClass().getMethod("getNameAliases").invoke(parentClass), false, s -> {
                    s = s.toLowerCase().replaceAll("[-_ ]", "");
                    for (String sub : s.split("\n")) {
                        if (finalQuery.contains(sub))
                            return true;
                    }
                    return false;
                }));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return false;
            }
        } else
            return false;
    }

    static boolean equalsQuery(ParentClass parentClass, String query, boolean ignoreCase) {
        if (ignoreCase && parentClass.getName().equalsIgnoreCase(query))
            return true;
        else if (!ignoreCase && parentClass.getName().equals(query))
            return true;
        else if (parentClass instanceof ParentClass_Alias) {
            try {
                return Utility.isNullReturnOrElse((String) parentClass.getClass().getMethod("getNameAliases").invoke(parentClass), false, s -> {
                    return Arrays.stream(s.split("\n")).anyMatch(alias -> {
                        if (ignoreCase)
                            return alias.equalsIgnoreCase(query);
                        else
                            return alias.equals(query);
                    });
                });
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return false;
            }
        } else
            return false;


    }
}

package com.pkhh.projectcndd.utils;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.annimon.stream.Stream;
import com.pkhh.projectcndd.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import timber.log.Timber;

public class LanguageUtil {
  private LanguageUtil() {}

  /**
   * check language exist in SharedPrefs, if not exist then default language is Vietnamese
   */
  public static Language getCurrentLanguage(Context context) {
    String currentLanguageCode = SharedPrefUtil.getInstance(context).getLanguageCode(context.getString(R.string.language_vietnamese_code));
    final List<Language> allLanguages = getAllLanguages(context);
    Timber.tag("%%%").d("%s %s", currentLanguageCode, allLanguages);
    return Stream.of(allLanguages)
        .filter(i -> Objects.equals(i.code, currentLanguageCode))
        .findFirst()
        .orElseThrow();
  }

  /**
   * return language list from string.xml
   */
  public static List<Language> getAllLanguages(Context context) {
    List<Language> languageList = new ArrayList<>();
    List<String> languageNames =
        Arrays.asList(context.getResources().getStringArray(R.array.language_names));
    List<String> languageCodes =
        Arrays.asList(context.getResources().getStringArray(R.array.language_codes));
    if (languageNames.size() != languageCodes.size()) {
      // error, make sure these arrays are same size
      return languageList;
    }
    for (int i = 0, size = languageNames.size(); i < size; i++) {
      languageList.add(new Language(i,  languageCodes.get(i), languageNames.get(i)));
    }
    return languageList;
  }

  /**
   * load current locale and change language
   */
  public static void loadLocale(Context context) {
    changeLanguage(context, getCurrentLanguage(context));
  }

  /**
   * change app language
   */
  public static void changeLanguage(Context context, Language language) {
    SharedPrefUtil.getInstance(context).saveLanguageCode(language.code);
    Locale locale = new Locale(language.code);
    Resources resources = context.getResources();
    Configuration configuration = resources.getConfiguration();
    configuration.setLocale(locale);
    resources.updateConfiguration(configuration, resources.getDisplayMetrics());
  }
}

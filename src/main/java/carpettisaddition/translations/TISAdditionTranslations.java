package carpettisaddition.translations;

//#if MC >= 11500
import carpet.CarpetSettings;
//#else
//$$ import carpettisaddition.utils.compat.carpet.CarpetSettings;
//#endif

import carpettisaddition.CarpetTISAdditionServer;
import carpettisaddition.CarpetTISAdditionSettings;
import carpettisaddition.mixins.translations.StyleAccessor;
import carpettisaddition.utils.FileUtil;
import carpettisaddition.utils.Messenger;
import com.google.common.collect.Maps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TISAdditionTranslations
{
	public static final String DEFAULT_LANGUAGE = "en_us";
	public static final String TRANSLATION_NAMESPACE = CarpetTISAdditionServer.compactName;  // "carpettisaddition"
	public static final String TRANSLATION_KEY_PREFIX = TRANSLATION_NAMESPACE + ".";  // "carpettisaddition."
	public static final String CARPET_TRANSLATIONS_KEY_PREFIX = TRANSLATION_KEY_PREFIX + "carpet_translations.";  // "carpettisaddition.carpet_translations."
	private static final String RESOURCE_DIR = String.format("assets/%s/lang", TRANSLATION_NAMESPACE);

	@VisibleForTesting
	public static final Map<String, Map<String, String>> translationStorage = Maps.newLinkedHashMap();

	@NotNull
	public static Map<String, String> getTranslationFromResourcePath(String lang)
	{
		return translationStorage.getOrDefault(lang, Collections.emptyMap());
	}

	@SuppressWarnings("unchecked")
	public static void loadTranslations()
	{
		try
		{
			String dataStr = FileUtil.readResourceFileAsString(RESOURCE_DIR + "/meta/languages.yml");
			Map<String, Object> yamlMap = new Yaml().load(dataStr);
			((List<String>)yamlMap.get("languages")).forEach(TISAdditionTranslations::loadTranslation);
		}
		catch (Exception e)
		{
			CarpetTISAdditionServer.LOGGER.error("Failed to read language list", e);
		}
	}

	private static void loadTranslation(String lang)
	{
		translationStorage.computeIfAbsent(lang, l -> {
			String dataStr;
			try
			{
				dataStr = FileUtil.readResourceFileAsString(String.format("%s/%s.yml", RESOURCE_DIR, lang));
			}
			catch (IOException e)
			{
				return Collections.emptyMap();
			}
			try
			{
				Map<String, Object> yamlMap = new Yaml().load(dataStr);
				Map<String, String> translation = Maps.newLinkedHashMap();
				build(translation, yamlMap, "");
				return translation;
			}
			catch (Exception e)
			{
				CarpetTISAdditionServer.LOGGER.error("Failed to load translation of language " + lang, e);
				if (FabricLoader.getInstance().isDevelopmentEnvironment())
				{
					throw e;
				}
				return Collections.emptyMap();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private static void build(Map<String, String> translation, Map<String, Object> yamlMap, String prefix)
	{
		yamlMap.forEach((key, value) -> {
			String fullKey = prefix.isEmpty() ? key : (!key.equals(".") ? prefix + "." + key : prefix);
			if (value instanceof String)
			{
				translation.put(fullKey, (String)value);
			}
			else if (value instanceof Map)
			{
				build(translation, (Map<String, Object>)value, fullKey);
			}
			else
			{
				throw new RuntimeException(String.format("Unknown type %s in with key %s", value.getClass(), fullKey));
			}
		});
	}

	public static String getServerLanguage()
	{
		return CarpetSettings.language.equalsIgnoreCase("none") ? DEFAULT_LANGUAGE : CarpetSettings.language;
	}

	/**
	 * key -> translated formatting string
	 */
	@Nullable
	public static String translateKeyToFormattingString(String lang, String key)
	{
		return getTranslationFromResourcePath(lang.toLowerCase()).get(key);
	}

	public static BaseText translate(BaseText text, String lang)
	{
		return translateText(text, lang);
	}

	public static BaseText translate(BaseText text)
	{
		return translate(text, getServerLanguage());
	}

	public static BaseText translate(BaseText text, ServerPlayerEntity player)
	{
		if (CarpetTISAdditionSettings.ultraSecretSetting.equals("translation"))
		{
			return translate(text);
		}
		return translate(text, ((ServerPlayerEntityWithClientLanguage)player).getClientLanguage$TISCM());
	}

	private static BaseText translateText(BaseText text, @NotNull String lang)
	{
		// quick scan to check if any required translation exists
		boolean[] translationRequired = new boolean[]{false};
		forEachTISCMTranslationText(text, lang, (txt, msgKeyString) -> {
			translationRequired[0] = true;
			return txt;
		});
		if (!translationRequired[0])
		{
			return text;
		}

		// make a copy of the text, and apply translation
		return forEachTISCMTranslationText(Messenger.copy(text), lang, (txt, msgKeyString) -> {

			//#if MC >= 11900
			//$$ TranslatableTextContent content = (TranslatableTextContent) txt.getContent();
			//$$ String txtKey = content.getKey();
			//$$ Object[] txtArgs = content.getArgs();
			//#else
			String txtKey = txt.getKey();
			Object[] txtArgs = txt.getArgs();
			//#endif

			if (msgKeyString == null)
			{
				CarpetTISAdditionServer.LOGGER.warn("TISCM: Unknown translation key {}", txtKey);
				return txt;
			}

			BaseText newText;
			try
			{
				newText = Messenger.format(msgKeyString, txtArgs);
			}
			catch (IllegalArgumentException e)
			{
				newText = Messenger.s(msgKeyString);
			}

			// migrating text data
			newText.getSiblings().addAll(txt.getSiblings());
			newText.setStyle(txt.getStyle());

			return newText;
		});
	}

	private static BaseText forEachTISCMTranslationText(BaseText text, @NotNull String lang, TextModifier modifier)
	{
		if (
				//#if MC >= 11900
				//$$ text.getContent() instanceof TranslatableTextContent
				//#else
				text instanceof TranslatableText
				//#endif
		)
		{
			//#if MC >= 11900
			//$$ TranslatableTextContent translatableText = (TranslatableTextContent)text.getContent();
			//#else
			TranslatableText translatableText = (TranslatableText)text;
			//#endif

			// translate arguments
			Object[] args = translatableText.getArgs();
			for (int i = 0; i < args.length; i++)
			{
				Object arg = args[i];
				if (arg instanceof BaseText)
				{
					BaseText newText = forEachTISCMTranslationText((BaseText)arg, lang, modifier);
					if (newText != arg)
					{
						args[i] = newText;
					}
				}
			}

			// do translation logic
			if (translatableText.getKey().startsWith(TRANSLATION_KEY_PREFIX))
			{
				String msgKeyString = translateKeyToFormattingString(lang, translatableText.getKey());
				if (msgKeyString == null && !lang.equals(DEFAULT_LANGUAGE))
				{
					msgKeyString = translateKeyToFormattingString(DEFAULT_LANGUAGE, translatableText.getKey());
				}
				text = modifier.apply(
						//#if MC >= 11900
						//$$ text,
						//#else
						translatableText,
						//#endif
						msgKeyString
				);
			}
		}

		// translate hover text
		HoverEvent hoverEvent = ((StyleAccessor)text.getStyle()).getHoverEventField();
		if (hoverEvent != null)
		{
			//#if MC >= 11600
   //$$
			//$$ Object hoverText = hoverEvent.getValue(hoverEvent.getAction());
			//$$ if (hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT && hoverText instanceof BaseText)
			//$$ {
			//$$ 	 BaseText newText = forEachTISCMTranslationText((BaseText)hoverText, lang, modifier);
			//$$ 	 if (newText != hoverText)
			//$$ 	 {
			//$$ 		 text.setStyle(text.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newText)));
			//$$ 	 }
			//$$ }
			//#else
			Text hoverText = hoverEvent.getValue();
			BaseText newText = forEachTISCMTranslationText((BaseText)hoverText, lang, modifier);
			if (newText != hoverText)
			{
				text.getStyle().setHoverEvent(new HoverEvent(hoverEvent.getAction(), newText));
			}
			//#endif
		}

		// translate sibling texts
		List<Text> siblings = text.getSiblings();
		for (int i = 0; i < siblings.size(); i++)
		{
			Text sibling = siblings.get(i);
			BaseText newText = forEachTISCMTranslationText((BaseText)sibling, lang, modifier);
			if (newText != sibling)
			{
				siblings.set(i, newText);
			}
		}
		return text;
	}

	@FunctionalInterface
	private interface TextModifier
	{
		BaseText apply(
				//#if MC >= 11900
				//$$ MutableText translatableText,
				//#else
				TranslatableText translatableText,
				//#endif
				@Nullable String msgKeyString
		);
	}
}

package br.com.jpb.liwc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import br.com.jpb.util.StringUtil;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;

public class Liwc {

	private static final Set<String> POSITIVE_EMOTICONS = new HashSet<>(
			Arrays.asList(":-)", ":)", ":D", ":o)", ":]", ":3", ":c)", ":>",
					"=]", "8)",
					"=)", ":}", ":^)", ":?)", ":-)", ":-D", "8-D", "8D", "x-D",
					"xD", "X-D", "XD", "=-D", "=D", "=-3", "=3", "B^D", ":-))",
					":'-)", ":')",
					":*", ":^*", ";-)", ";)", "*-)", "*)", ";-]", ";]", ";D",
					";^)", ":-,", ">:P", ":-P", ":P", "X-P", "x-p", "xp", "XP",
					":-p", ":p", "=p",
					":-Þ", ":Þ", ":þ", ":-þ", ":-b", ":b", "d:", "O:-)",
					"0:-3", "0:3", "0:-)", "0:)", "0;^)", ">:)", ">;)", ">:-)",
					"}:-)", "}:)", "3:-)",
					"3:)", "o/\\o", "|;-)", "|-O", "#-)", "%-)", "%)", "\\o/",
					"*\0/*"));

	private static final Set<String> NEGATIVE_EMOTICONS = new HashSet<>(
			Arrays.asList(">:[", ":-(", ":(", "", ":-c", ":c", ":-<", "",
					":?C", ":<",
					":-[", ":[", ":{", ";(", ":-||", ":@", ">:(", ":'-(",
					":'(", "D:<", "D:", "D8", "D;", "D=", "DX", "v.v", "D-':",
					">:\\", ">:/", ":-/",
					":/", ":\\", "=/", "=\\", ":L", "=L", ":S", ">.<", ":|",
					":-|", ":$", ":-X", ":X", ":-#", ":#", "<:-|", "(>_<)",
					"(-_-)"));

	private static final Set<String> NEGATION_WORDS = new HashSet<>(
			Arrays.asList("JAMAIS", "NADA", "NEM", "NENHUM", "NINGUÉM",
					"NUNCA", "NÃO",
					"TAMPOUCO"));
	private static final Set<String> AMPLIFIER_WORDS = new HashSet<>(
			Arrays.asList("MAIS", "MUITO", "DEMAIS", "COMPLETAMENTE",
					"ABSOLUTAMENTE",
					"TOTALMENTE", "DEFINITIVAMENTE", "EXTREMAMENTE",
					"FREQUENTEMENTE", "BASTANTE"));
	private static final Set<String> REDUCER_WORDS = new HashSet<>(
			Arrays.asList("POUCO", "QUASE", "MENOS", "APENAS"));

	private static final BigDecimal THREE = BigDecimal.valueOf(3);
	private static final int DIC_START_LINE = 66;
	private static final String DIC_CLASS_LOADER_RESOURCE = "LIWC2007_Portugues_win.dic";
	private static final int DEFAULT_SCALE = 6;
	private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
	private static final Splitter SENTENCE_SPLITTER = Splitter.on(". ")
			.omitEmptyStrings().trimResults();
	private static final Splitter WORD_SPLITTER = Splitter
			.on(CharMatcher.WHITESPACE).omitEmptyStrings().trimResults();
	private static final BigDecimal POSITIVE_POLARITY = BigDecimal.valueOf(1)
			.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
	private static final BigDecimal NEGATIVE_POLARITY = BigDecimal.valueOf(-1)
			.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);

	private Map<String, Set<Category>> patternsWithCategories = new HashMap<>();

	public Liwc() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					Liwc.class.getClassLoader().getResourceAsStream(
							DIC_CLASS_LOADER_RESOURCE),
					Charsets.ISO_8859_1));
			String nextLine = null;
			int count = 0;
			while ((nextLine = br.readLine()) != null) {
				if (++count >= DIC_START_LINE) {
					String[] split = nextLine.split("\t");
					String word = split[0];
					final Set<Category> categories = new HashSet<>();
					for (int i = 1; i < split.length; i++) {
						Category category = Category.getByCod(Integer
								.parseInt(split[i]));
						categories.add(category);
					}
					patternsWithCategories.put(word.toUpperCase(), categories);
				}
			}
			br.close();
		} catch (IOException e) {
			throw new IllegalStateException(
					"Error while loading dictionary from "
							+ DIC_CLASS_LOADER_RESOURCE, e);
		}
	}

	public Liwc withDictionary(Map<String, Set<Category>> dictionary) {
		patternsWithCategories = new HashMap<>(dictionary);
		return this;
	}

	/**
	 * ALGORITHM:
	 * 
	 * 1: overall sentiment ← 0
	 * 2: while there is sentiment word in text do
	 * 3: polarity ← read lexicon(sentiment word)
	 * 4: if booster word in context then
	 * 5: if negation word in context then
	 * 6: polarity ← polarity/3
	 * 7: else
	 * 8: polarity ← polarity * 3
	 * 9: end if
	 * 10: else if reducer word in context then
	 * 11: if negation word in context then
	 * 12: polarity ← polarity * 3
	 * 13: else
	 * 14: polarity ← polarity/3
	 * 15: end if
	 * 16: else if negation word in context then
	 * 17: polarity ← −1 ∗ polarity
	 * 18: end if
	 * 19: overall sentiment = overall sentiment + polarity
	 * 20: end while
	 *
	 * @param text
	 * @return
	 */
	public BigDecimal sentimentOfText(final String text) {
		final Iterable<Iterable<String>> wordsOfSentences = FluentIterable
				.from(FluentIterable.from(SENTENCE_SPLITTER.split(text))
						.toList())
				.transform(new Function<String, Iterable<String>>() {
					@Override
					public Iterable<String> apply(String sentence) {
						return WORD_SPLITTER.split(sentence);
					}
				});

		BigDecimal sentiment = BigDecimal.ZERO.setScale(DEFAULT_SCALE,
				DEFAULT_ROUNDING_MODE);
		for (Iterable<String> sentence : wordsOfSentences) {
			boolean foundNegationWord = false;
			boolean foundAmplifierWord = false;
			boolean foundReducerWord = false;
			for (String word : sentence) {
				word = StringUtil.removePunctuation(word).replaceAll("\"", "")
						.replaceAll("'", "").replaceAll("`", "").toUpperCase();
				if (word.isEmpty()) {
					continue;
				}
				if (NEGATION_WORDS.contains(word)) {
					foundNegationWord = true;
					continue;
				}
				if (AMPLIFIER_WORDS.contains(word)) {
					foundAmplifierWord = true;
					continue;
				}
				if (REDUCER_WORDS.contains(word)) {
					foundReducerWord = true;
					continue;
				}

				BigDecimal polarity = polarityOfWord(word);
				if (polarity != null) {
					polarity = adjustPolarity(foundNegationWord,
							foundAmplifierWord, foundReducerWord, polarity);

					foundNegationWord = false;
					foundAmplifierWord = false;
					foundReducerWord = false;
					sentiment = sentiment.add(polarity);
				}
			}
		}
		return sentiment;
	}

	private BigDecimal adjustPolarity(boolean foundNegationWord,
			boolean foundAmplifierWord, boolean foundReducerWord,
			BigDecimal polarity) {
		if (foundAmplifierWord) {
			if (foundNegationWord) {
				polarity = polarity.divide(THREE, DEFAULT_SCALE,
						DEFAULT_ROUNDING_MODE);
			} else {
				polarity = polarity.multiply(THREE);
			}
		} else {
			if (foundReducerWord) {
				if (foundNegationWord) {
					polarity = polarity.multiply(THREE);
				} else {
					polarity = polarity.divide(THREE, DEFAULT_SCALE,
							DEFAULT_ROUNDING_MODE);
				}
			} else {
				if (foundNegationWord) {
					polarity = polarity.multiply(NEGATIVE_POLARITY);
				}
			}
		}
		return polarity;
	}

	private BigDecimal polarityOfWord(String word) {
		// first check for emoticons
		if (POSITIVE_EMOTICONS.contains(word)) {
			return POSITIVE_POLARITY;
		}
		if (NEGATIVE_EMOTICONS.contains(word)) {
			return NEGATIVE_POLARITY;
		}
		Set<Category> categories = patternsWithCategories.get(word);
		return Category.getPolarity(categories);
	}

	private enum Category {

		FUNCT(1),
		PRONOUN(2),
		PPRON(3),
		I(4),
		WE(5),
		YOU(6),
		SHEHE(7),
		THEY(8),
		IPRON(9),
		ARTICLE(10),
		VERB(11),
		AUXVERB(12),
		PAST(13),
		PRESENT(14),
		FUTURE(15),
		ADVERB(16),
		PREPS(17),
		CONJ(18),
		NEGATE(19),
		QUANT(20),
		NUMBER(21),
		SWEAR(22),
		SOCIAL(121),
		FAMILY(122),
		FRIEND(123),
		HUMANS(124),
		AFFECT(125),
		POSEMO(126),
		NEGEMO(127),
		ANX(128),
		ANGER(129),
		SAD(130),
		COGMECH(131),
		INSIGHT(132),
		CAUSE(133),
		DISCREP(134),
		TENTAT(135),
		CERTAIN(136),
		INHIB(137),
		INCL(138),
		EXCL(139),
		PERCEPT(140),
		SEE(141),
		HEAR(142),
		FEEL(143),
		BIO(146),
		BODY(147),
		HEALTH(148),
		SEXUAL(149),
		INGEST(150),
		RELATIV(250),
		MOTION(251),
		SPACE(252),
		TIME(253),
		WORK(354),
		ACHIEVE(355),
		LEISURE(356),
		HOME(357),
		MONEY(358),
		RELIG(359),
		DEATH(360),
		ASSENT(462),
		NONFL(463),
		FILLER(464);

		private final int cod;

		private Category(int cod) {
			this.cod = cod;
		}

		public static Category getByCod(final int cod) {
			return Stream.of(values()).filter(category -> category.cod == cod)
					.findFirst().orElse(null);
		}

		public static BigDecimal getPolarity(Set<Category> categoriesOfWord) {
			if (categoriesOfWord == null || categoriesOfWord.isEmpty()) {
				return null;
			}
			if (categoriesOfWord.contains(POSEMO)) {
				return POSITIVE_POLARITY;
			}
			if (categoriesOfWord.contains(NEGEMO)) {
				return NEGATIVE_POLARITY;
			}
			return null;
		}
	}

}
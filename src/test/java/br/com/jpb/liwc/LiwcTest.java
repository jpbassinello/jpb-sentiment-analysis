package br.com.jpb.liwc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LiwcTest {

	@Test
	public void sentimentOfTextTest() {
		Liwc liwc = new Liwc();
		Assert.assertEquals(new BigDecimal("1.333333"), liwc.sentimentOfText("O celular é bom, apesar da bateria não ser muito boa."));
		Assert.assertEquals(new BigDecimal("-0.333333"),
				liwc.sentimentOfText("O celular não é muito ruim, mas é um pouco lento e as vezes apaga de repente"));

		List<String> tests = Arrays
				.asList("RT @automotivas: BMW amplia linha Active Flex nas Séries 1 e 3 e também no X1 http://t.co/NFgGNVJyhC",
						"Celso Daniel, Toninho do PT e Eduardo Campos quebraram alianças com o PT... estranho..",
						"Ficou pequeno para as motocas !!!!!",
						"to de cara",
						"RT @RenanTvsilva: Jesuíta Barbosa posa ao lado de Fábio Audi e seguidores comentam: ‘Casal lindo, parabéns ' http://t.co/kOi30RUx7P http://…",
						"Vem audi,tem bastante balões pra encher kk",
						"Quero é pegar agora num Audi ou Seat leon",
						"Audi inaugura centro de vendas em Teresina, Local será o primeiro ponto de venda e atendimento da marca...",
						"Acabei de disputar em ESTÁGIO 1! Cheguei em 1º no meu Audi R8 V10 Spyder!",
						"RT @automotivas: Séries 1 e 3 da BMW tem novidades em suas versões no Brasil http://t.co/ZUnuyvKXmu",
						"RT @portalvox: Corpo de Eduardo Campos ficou pulverizado http://t.co/XxldwqRrpO",
						"Hahaha de mim tu não levou os 100pila Adriana Silveira e comigo levo mais três Alinee Mychael primaaaa	Allan Antthony Primooo Eduardo Silva do audi preto do Xangai E bora mete pressao que se nao quero 100zao na minha mao... Vcs tem ate domingo...",
						"#Eriketa#Audi #Ratu @loremata lorena ai esta a foto da sua irma....kkkk http://t.co/rbxicjdtb0",
						"Muito top #BMW",
						"OFERTO!!  Nissan Terrano PR50 Turbo diesel 2.7 (diesel comun)... Gs.27.000.000, Cédula verde a transferir.-",
						"Já gostei mais do bmw A1 que agora",
						"RT @marcelasd_: Eu nasci pra brilhar dentro de uma BMW à 200 por hora, e não dentro de um ônibus que para a cada 10 metros",
						"Leve o estilo das pistas de corrida para o seu dia a dia! Jaqueta BMW a partir de R$169,90: http://t.co/yKda54Dh3V http://t.co/725J5an2Hg",
						"Deus é o meu guia 🙏☝️👊 #country #BMW #america #deusnosabencoe #countrylife http://t.co/2bh3f8ZSIj",
						"Veio né libero a bmw devolta",
						"RT @frasesdfanfic: – Você me causou vários danos... Não só morais, mas também materiais, já que a minha BMW virou ferro velho por sua causa…");

		for (String test : tests) {
			System.out.println("Text: " + test);
			System.out.println("Polarity: " + liwc.sentimentOfText(test));
			System.out.println();
		}
	}

}

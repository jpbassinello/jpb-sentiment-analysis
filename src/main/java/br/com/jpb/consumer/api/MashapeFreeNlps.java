package br.com.jpb.consumer.api;

import com.google.common.base.Charsets;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import br.com.jpb.util.StringUtil;

public class MashapeFreeNlps {

	private static final String URL = "https://loudelement-free-natural-language-processing-service.p.mashape.com/nlp-text/?text=";

	public static void main(String[] args) throws UnirestException {

		String test = StringUtil.urlEncode(
				"Estou novamente aqui para agradecer pela atenção . assim que eu reclamei vcs me deram uma resposta ... Tudo foi um engano pois o seu site esta com erro ele nao esta atualizando a entrega .. Fila relatando que esta preparando para envio quando já entregou... Foram um erro de vocês . Obrigado",
				Charsets.UTF_8.displayName());

		HttpResponse<JsonNode> response = Unirest.get(URL + test)
				.header("X-Mashape-Key", "fXFjwVKuYlmshi8uZcAdiubDeYjKp1Y8sWTjsn9rnk48TgB4fV")
				.header("Accept", "application/json").asJson();
		System.out.println(response.getBody());
	}

}

package com.sapuseven.untis.data.connectivity

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.TextUtils
import com.sapuseven.untis.models.untis.params.BaseParams
import kotlinx.serialization.Serializable
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder

class UntisRequest {
	suspend fun request(query: UntisRequestQuery): Result<String, FuelError> {
		return query.getURI("utf-8").toString().httpPost()
				.header(mapOf("Content-Type" to "application/json; charset=UTF-8"))
				.body(getJSON().stringify(UntisRequestData.serializer(), query.data))
				.awaitStringResult()
	}

	class UntisRequestQuery {
		var url = ""
		var school = ""
		var data: UntisRequestData = UntisRequestData()

		@Throws(URISyntaxException::class, UnsupportedEncodingException::class)
		internal fun getURI(encoding: String): URI {
			return if (!TextUtils.isNullOrEmpty(school) && !TextUtils.isNullOrEmpty(data.method))
				URI(url + "?school=" + URLEncoder.encode(school, encoding) + "&m=" + URLEncoder.encode(data.method, encoding))
			else if (!TextUtils.isNullOrEmpty(school))
				URI(url + "?school=" + URLEncoder.encode(school, encoding))
			else if (!TextUtils.isNullOrEmpty(data.method))
				URI(url + "?m=" + URLEncoder.encode(data.method, encoding))
			else
				URI(url)
		}
	}

	@Serializable
	class UntisRequestData {
		var id: String = ""
		var jsonrpc: String = "2.0"
		var method: String = ""
		var params: List<BaseParams> = emptyList()
	}
}

/*private CachingMode cachingMode = LOAD_LIVE;
private int startDateFromWeek;
private JSONObject cacheFallback;
private SessionInfo sessionInfo;

@Override
protected JSONObject doInBackground(UntisRequestQuery... query) {
	boolean cacheExists = false;

	if (cachingMode != LOAD_LIVE) {
		ListManager listManager = new ListManager(context.get());

		MasterData masterData = null;
		try {
			masterData = new MasterData(ListManager.getUserData(context.get()).getJSONObject("masterData"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		TimegridUnitManager unitManager = new TimegridUnitManager(masterData);

		String fileName = sessionInfo.getElemType() + "-"
				+ sessionInfo.getElemId() + "-"
				+ startDateFromWeek + "-"
				+ addDaysToInt(startDateFromWeek, unitManager.getNumberOfDays() - 1);

		cacheExists = listManager.exists(fileName, true);

		if (cacheExists)
			try {
				switch (cachingMode) {
					case RETURN_CACHE:
						return new JSONObject(listManager.readList(fileName, true));
					case LOAD_LIVE_FALLBACK_CACHE:
						cacheFallback = new JSONObject(listManager.readList(fileName, true));
						break;
					default:
						publishProgress(new JSONObject(listManager.readList(fileName, true)));
						break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				if (cachingMode == RETURN_CACHE)
					return null;
			}
	}

	try {
		URL url = new URL(query[0].getURI("utf-8").toString());

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		connection.setDoOutput(true);
		connection.connect();

		OutputStream out = connection.getOutputStream();

		out.write(new JSONObject()
				.put("id", 0)
				.put("method", query[0].getMethod())
				.put("params", query[0].getParams())
				.put("jsonrpc", query[0].getJsonrpc())
				.toString().getBytes());
		out.flush();
		out.close();

		int statusCode = connection.getResponseCode();
		if (statusCode != HttpURLConnection.HTTP_OK) {
			//if (cachingMode == LOAD_LIVE)
			return new JSONObject()
					.put("id", 0)
					.put("error", new JSONObject()
							.put("code", 1)//Constants.UntisAPI.ERROR_CODE_UNKNOWN)
							.put("message", "Unexpected status code: " + statusCode)
					);
			else if (cachingMode == LOAD_LIVE_FALLBACK_CACHE && cacheFallback != null)
				return cacheFallback;
			return null;
		}

		InputStream inputStream = connection.getInputStream();

		if (inputStream != null) {
			//if (cachingMode != RETURN_CACHE_LOAD_LIVE
			//|| (cachingMode == RETURN_CACHE_LOAD_LIVE && !cacheExists))
			return new JSONObject(readStream(inputStream))
					.put("timeModified", System.currentTimeMillis());
		} else {
			if (cachingMode == LOAD_LIVE_FALLBACK_CACHE && cacheFallback != null)
				return cacheFallback;
			else
			return null;
		}
	} catch (Exception e) {
		try {
			//if (cachingMode == LOAD_LIVE)
			return new JSONObject()
					.put("id", 0)
					.put("error", new JSONObject()
							.put("code", e instanceof UnknownHostException ?
									1 ://Constants.UntisAPI.ERROR_CODE_NO_SERVER_FOUND :
									1)//Constants.UntisAPI.ERROR_CODE_UNKNOWN)
							.put("message", e.getMessage())
					);
			else if (cachingMode == LOAD_LIVE_FALLBACK_CACHE && cacheFallback != null)
				return cacheFallback;
			else
				return null;
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	//return null;
}

@Override
protected void onProgressUpdate(JSONObject... responses) {
	if (context.get() == null)
		return;

	if (responses.length > 0)
		for (JSONObject response : responses)
			handler.onResponseReceived(response);
}

@Override
protected void onPostExecute(JSONObject response) {
	if (context.get() == null)
		return;

	handler.onResponseReceived(response);
}

public void submit(UntisRequestQuery query) {
	this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
}

private String readStream(InputStream is) {
	try {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		int i = is.read();
		while (i != -1) {
			bo.write(i);
			i = is.read();
		}
		return bo.toString();
	} catch (IOException e) {
		e.printStackTrace();
		return "";
	}
}

public UntisRequest setResponseHandler(UntisRequest.ResponseHandler handler) {
	this.handler = handler;
	return this;
}

public void setCachingMode(CachingMode cachingMode) {
	//this.cachingMode = cachingMode;
}

public enum CachingMode {
	RETURN_CACHE,
	RETURN_CACHE_LOAD_LIVE,
	RETURN_CACHE_LOAD_LIVE_RETURN_LIVE,
	LOAD_LIVE,
	LOAD_LIVE_FALLBACK_CACHE
}

public interface ResponseHandler {
	void onResponseReceived(JSONObject response);
}*/

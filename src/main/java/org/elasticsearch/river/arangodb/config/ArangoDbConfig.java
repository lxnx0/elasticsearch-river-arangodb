package org.elasticsearch.river.arangodb.config;

import static java.util.Collections.unmodifiableSet;
import static net.swisstech.swissarmyknife.lang.Integers.positive;
import static net.swisstech.swissarmyknife.lang.Longs.positive;
import static net.swisstech.swissarmyknife.lang.Strings.notBlank;
import static net.swisstech.swissarmyknife.net.TCP.validPortNumber;
import static net.swisstech.swissarmyknife.util.Sets.newHashSet;
import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.elasticsearch.common.unit.TimeValue.timeValueMillis;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import net.swisstech.arangodb.model.wal.WalEvent;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Singleton;
import org.elasticsearch.river.RiverIndexName;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.script.ScriptService.ScriptType;

/** config object and holder of shared object */
@Singleton
public class ArangoDbConfig {

	private final String riverIndexName;
	private final String riverName;
	private final BlockingQueue<WalEvent> eventStream;
	private final String arangodbHost;
	private final int arangodbPort;
	private final String arangodbDatabase;
	private final String arangodbCollection;
	private final ExecutableScript arangodbScript;
	private final boolean arangodbFullSync;
	private final boolean arangodbDropcollection;
	private final long arangodbMinWait;
	private final long arangodbMaxWait;
	private final Set<String> arangodbOptionsExcludeFields;
	private final String arangodbCredentialsUsername;
	private final String arangodbCredentialsPassword;
	private final String indexName;
	private final String indexType;
	private final int indexBulkSize;
	private final int indexThrottleSize;
	private final long indexBulkTimeout;

	@Inject
	public ArangoDbConfig( //
	RiverSettingsWrapper rsw, //
		@RiverIndexName final String riverIndexName, //
		RiverName pRiverName, //
		ScriptService scriptService //
	) {

		this.riverIndexName = riverIndexName;
		riverName = pRiverName.name();

		// arangodb
		arangodbHost = notBlank(rsw.getString("arangodb.host", "localhost"));
		arangodbPort = validPortNumber(rsw.getInteger("arangodb.port", 8529));
		arangodbDatabase = notBlank(rsw.getString("arangodb.db", riverName));
		arangodbCollection = notBlank(rsw.getString("arangodb.collection", riverName));

		String scriptString = rsw.getString("arangodb.script", null);
		String scriptLang = notBlank(rsw.getString("arangodb.scriptType", "js"));
		arangodbScript = scriptService.executable(scriptLang, scriptString, ScriptType.INLINE, newHashMap());

		// arangodb.options
		arangodbFullSync = rsw.getBool("arangodb.full_sync", false);
		arangodbDropcollection = rsw.getBool("arangodb.drop_collection", true);
		arangodbMinWait = positive(rsw.getTimeValue("arangodb.min_wait", timeValueMillis(100)).millis());
		arangodbMaxWait = positive(rsw.getTimeValue("arangodb.max_wait", timeValueMillis(10_000)).millis());

		Set<String> excludes = newHashSet("_id", "_key", "_rev");
		excludes.addAll(rsw.getList("arangodb.options.exclude_fields"));
		arangodbOptionsExcludeFields = unmodifiableSet(excludes);

		// arangodb.credentials
		arangodbCredentialsUsername = notBlank(rsw.getString("arangodb.credentials.username", ""));
		arangodbCredentialsPassword = notBlank(rsw.getString("arangodb.credentials.password", ""));

		// index
		indexName = notBlank(rsw.getString("index.name", riverName));
		indexType = notBlank(rsw.getString("index.type", riverName));
		indexBulkSize = positive(rsw.getInteger("index.bulk_size", 100));
		indexThrottleSize = rsw.getInteger("index.throttle_size", indexBulkSize * 5);
		indexBulkTimeout = positive(rsw.getTimeValue("index.bulk_timeout", timeValueMillis(10)).millis());

		// event stream from producer to consumer
		if (indexThrottleSize == -1) {
			eventStream = new LinkedTransferQueue<WalEvent>();
		}
		else {
			eventStream = new ArrayBlockingQueue<WalEvent>(indexThrottleSize);
		}
	}

	public String getRiverIndexName() {
		return riverIndexName;
	}

	public String getRiverName() {
		return riverName;
	}

	public BlockingQueue<WalEvent> getEventStream() {
		return eventStream;
	}

	public String getArangodbHost() {
		return arangodbHost;
	}

	public int getArangodbPort() {
		return arangodbPort;
	}

	public String getArangodbDatabase() {
		return arangodbDatabase;
	}

	public String getArangodbCollection() {
		return arangodbCollection;
	}

	public ExecutableScript getArangodbScript() {
		return arangodbScript;
	}

	public boolean getArangodbFullSync() {
		return arangodbFullSync;
	}

	public boolean getArangodbDropcollection() {
		return arangodbDropcollection;
	}

	public long getArangodbMinWait() {
		return arangodbMinWait;
	}

	public long getArangodbMaxWait() {
		return arangodbMaxWait;
	}

	public Set<String> getArangodbOptionsExcludeFields() {
		return arangodbOptionsExcludeFields;
	}

	public String getArangodbCredentialsUsername() {
		return arangodbCredentialsUsername;
	}

	public String getArangodbCredentialsPassword() {
		return arangodbCredentialsPassword;
	}

	public String getIndexName() {
		return indexName;
	}

	public String getIndexType() {
		return indexType;
	}

	public int getIndexBulkSize() {
		return indexBulkSize;
	}

	public int getIndexThrottleSize() {
		return indexThrottleSize;
	}

	public long getIndexBulkTimeout() {
		return indexBulkTimeout;
	}
}
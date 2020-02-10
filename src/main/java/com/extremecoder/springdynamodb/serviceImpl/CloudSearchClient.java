package com.extremecoder.springdynamodb.serviceImpl;

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainAsyncClient;
import com.amazonaws.services.cloudsearchdomain.model.*;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearchAsync;
import com.amazonaws.services.cloudsearchv2.model.*;
import com.amazonaws.util.StringInputStream;
import com.extremecoder.springdynamodb.docs.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.amazonaws.services.cloudsearchdomain.model.ContentType.Applicationjson;

@Service
public class CloudSearchClient {

	private static final Logger log = LoggerFactory.getLogger(CloudSearchClient.class);

	public static final String NO_FIELDS = "_no_fields";
	public static final String ALL_FIELDS = "_all_fields";
	public static final String INITIAL_CURSOR = "initial";

	@Autowired
	private AmazonCloudSearchAsync cloudSearchAsyncClient;
	@Autowired
	private Map<String, AmazonCloudSearchDomainAsyncClient> cloudSearchDomainAsyncClients;

	@Value("${cloud-search.documentUploadMaxSizeBytes}")
	private int documentUploadMaxSizeBytes;
	private ProgressListener progressListener;

	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	public void setDocumentUploadMaxSizeBytes(int documentUploadMaxSizeBytes) {
		this.documentUploadMaxSizeBytes = documentUploadMaxSizeBytes;
	}

	public IndexDocumentsResult indexDocuments(String domainName) {
		return cloudSearchAsyncClient.indexDocuments(new IndexDocumentsRequest().withDomainName(domainName));
	}

	public DefineIndexFieldResult defineIndexField(DefineIndexFieldRequest defineIndexFieldRequest) {
		return cloudSearchAsyncClient.defineIndexField(defineIndexFieldRequest);
	}

	public DeleteIndexFieldResult deleteIndexField(DeleteIndexFieldRequest deleteIndexFieldRequest) {
		return cloudSearchAsyncClient.deleteIndexField(deleteIndexFieldRequest);
	}

	public DescribeDomainsResult describeDomain(String domainName) {
		DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest().withDomainNames(domainName);
		return cloudSearchAsyncClient.describeDomains(describeDomainsRequest);
	}

	public List<UploadDocumentsResult> uploadDocuments(String domainName, List<Document> docs) {
		if (docs.size() == 0) {
			return Collections.emptyList();
		}
		AmazonCloudSearchDomainAsyncClient domainClient = cloudSearchDomainAsyncClients.get(domainName);
		if (domainClient == null) {
			throw new IllegalArgumentException(domainName + " not known");
		}
		List<UploadDocumentsRequest> uploadDocumentsRequests = createUploadDocumentsRequest(docs);
		List<UploadDocumentsResult> uploadDocumentsResults = new ArrayList<>(uploadDocumentsRequests.size());
		for (UploadDocumentsRequest uploadDocumentsRequest : uploadDocumentsRequests) {
			UploadDocumentsResult uploadDocumentsResult = domainClient.uploadDocuments(uploadDocumentsRequest);
			uploadDocumentsResults.add(uploadDocumentsResult);
		}
		return uploadDocumentsResults;
	}

	public List<Future<UploadDocumentsResult>> uploadDocumentsAsync(String domainName, List<Document> docs) {
		if (docs.size() == 0) {
			return Collections.emptyList();
		}
		AmazonCloudSearchDomainAsyncClient domainClient = cloudSearchDomainAsyncClients.get(domainName);
		if (domainClient == null) {
			throw new IllegalArgumentException(domainName + " not known");
		}
		List<UploadDocumentsRequest> uploadDocumentsRequests = createUploadDocumentsRequest(docs);
		List<Future<UploadDocumentsResult>> uploadDocumentsResults = new ArrayList<>(uploadDocumentsRequests.size());
		for (UploadDocumentsRequest uploadDocumentsRequest : uploadDocumentsRequests) {
			Future<UploadDocumentsResult> uploadDocumentsResult = domainClient
					.uploadDocumentsAsync(uploadDocumentsRequest);
			uploadDocumentsResults.add(uploadDocumentsResult);
		}
		return uploadDocumentsResults;
	}

	private List<UploadDocumentsRequest> createUploadDocumentsRequest(List<Document> docs) {
		List<String> parts = chunkedJson(docs);
		List<UploadDocumentsRequest> uploadDocumentRequests = new ArrayList<>(parts.size());
		for (String part : parts) {
			try (StringInputStream documents = new StringInputStream(part)) {
				UploadDocumentsRequest uploadDocumentsRequest = new UploadDocumentsRequest(). //
						withDocuments(documents). //
						withContentLength((long) part.length()). //
						withContentType(Applicationjson);
				if (progressListener != null) {
					uploadDocumentsRequest.setGeneralProgressListener(progressListener);
				}
				uploadDocumentRequests.add(uploadDocumentsRequest);
			} catch (IOException e) {
				log.warn("this should never happen", e);
			}
		}
		return uploadDocumentRequests;
	}

	private List<String> chunkedJson(List<Document> docs) {
		List<String> parts = new ArrayList<>();
		int len = docs.size();
		StringBuffer sb = new StringBuffer("[");
		boolean appendComma = false;
		// TODO there's a bug in here when the chunk is too small
		for (int i = 0; i < len; i += 1) {
			String json = docs.get(i).toJsonString();
			log.trace(json);
			if (json.length() > documentUploadMaxSizeBytes) {
				// if a single object is larger than the desired size, still
				// send an array of size 1
				sb.append(json);
				sb.append("]");
				String part = sb.toString();
				log.debug("part {}", part.length());
				parts.add(part.toString());
				sb = new StringBuffer("[");
				appendComma = false;
			} else if (sb.length() + json.length() > documentUploadMaxSizeBytes) {
				// if this object would case the length to be greater than
				// the max, start a new one
				sb.append("]");
				String part = sb.toString();
				log.debug("part {}", part.length());
				parts.add(part.toString());
				sb = new StringBuffer("[");
				sb.append(json);
				log.trace("partial {}", sb.length());
				appendComma = true;
			} else {
				if (appendComma) {
					sb.append(',');
				}
				sb.append(json);
				log.trace("partial {}", sb.length());
				appendComma = true;
			}
		}
		// add the last chunk
		if (sb.charAt(sb.length() - 1) != ']') {
			sb.append("]");
		}
		String part = sb.toString();
		log.debug("part {}", part.length());
		parts.add(part.toString());
		return parts;
	}

	public List<SearchResult> search(String domainName, SearchRequest searchRequest) {
		AmazonCloudSearchDomainAsyncClient domainClient = cloudSearchDomainAsyncClients.get(domainName);
		if (domainClient == null) {
			throw new IllegalArgumentException(domainName + " not known");
		}
		if (progressListener != null) {
			searchRequest.setGeneralProgressListener(progressListener);
		}
		List<SearchResult> searchResults = new ArrayList<>();
		int found = 0;
		while (true) {
			SearchResult searchResult = domainClient.search(searchRequest);
			searchResults.add(searchResult);
			Hits hits = searchResult.getHits();
			log.debug("found {} {}", found, hits.getFound());
			int size = hits.getHit().size();
			found += size;
			if (size == 0 || hits.getFound() == found || hits.getCursor() == null) {
				break;
			}
			searchRequest.setCursor(hits.getCursor());
		}
		return searchResults;
	}

}

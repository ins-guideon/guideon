package com.guideon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "")
public class GuideonProperties {

    private Gemini gemini = new Gemini();
    private Cohere cohere = new Cohere();
    private Embedding embedding = new Embedding();
    private Vector vector = new Vector();
    private Reranking reranking = new Reranking();
    private Hybrid hybrid = new Hybrid();
    private Bm25 bm25 = new Bm25();
    private Rag rag = new Rag();
    private Storage storage = new Storage();
    private Vectorstore vectorstore = new Vectorstore();
    private Regulation regulation = new Regulation();
    private Mail mail = new Mail();

    public static class Mail {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private final Properties properties = new Properties();

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Properties getProperties() { return properties; }
    }

    public static class Gemini {
        private Api api = new Api();
        private Chat chat = new Chat();

        public static class Api {
            private String key;
            public String getKey() { return key; }
            public void setKey(String key) { this.key = key; }
        }

        public static class Chat {
            private String modelName = "gemini-2.5-flash";
            private Double temperature = 0.2;
            public String getModelName() { return modelName; }
            public void setModelName(String modelName) { this.modelName = modelName; }
            public Double getTemperature() { return temperature; }
            public void setTemperature(Double temperature) { this.temperature = temperature; }
        }

        public Api getApi() { return api; }
        public void setApi(Api api) { this.api = api; }
        public Chat getChat() { return chat; }
        public void setChat(Chat chat) { this.chat = chat; }
    }

    public static class Cohere {
        private Api api = new Api();
        public static class Api {
            private String key;
            public String getKey() { return key; }
            public void setKey(String key) { this.key = key; }
        }
        public Api getApi() { return api; }
        public void setApi(Api api) { this.api = api; }
    }

    public static class Embedding {
        private Model model = new Model();
        private Integer dimension = 768;
        public static class Model {
            private String name = "text-embedding-004";
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
        }
        public Model getModel() { return model; }
        public void setModel(Model model) { this.model = model; }
        public Integer getDimension() { return dimension; }
        public void setDimension(Integer dimension) { this.dimension = dimension; }
    }

    public static class Vector {
        private Search search = new Search();
        public static class Search {
            private Integer maxResults = 5;
            private Double minScore = 0.5;
            public Integer getMaxResults() { return maxResults; }
            public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }
            public Double getMinScore() { return minScore; }
            public void setMinScore(Double minScore) { this.minScore = minScore; }
        }
        public Search getSearch() { return search; }
        public void setSearch(Search search) { this.search = search; }
    }

    public static class Reranking {
        private Boolean enabled = false;
        private String modelName = "rerank-multilingual-v3.0";
        private Integer initialResults = 20;
        private Integer finalResults = 5;
        private Double minScore = 0.8;
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
        public Integer getInitialResults() { return initialResults; }
        public void setInitialResults(Integer initialResults) { this.initialResults = initialResults; }
        public Integer getFinalResults() { return finalResults; }
        public void setFinalResults(Integer finalResults) { this.finalResults = finalResults; }
        public Double getMinScore() { return minScore; }
        public void setMinScore(Double minScore) { this.minScore = minScore; }
    }

    public static class Hybrid {
        private Search search = new Search();
        public static class Search {
            private Boolean enabled = false;
            private Double vectorWeight = 0.5;
            private Double keywordWeight = 0.5;
            private Integer initialResults = 40;
            public Boolean getEnabled() { return enabled; }
            public void setEnabled(Boolean enabled) { this.enabled = enabled; }
            public Double getVectorWeight() { return vectorWeight; }
            public void setVectorWeight(Double vectorWeight) { this.vectorWeight = vectorWeight; }
            public Double getKeywordWeight() { return keywordWeight; }
            public void setKeywordWeight(Double keywordWeight) { this.keywordWeight = keywordWeight; }
            public Integer getInitialResults() { return initialResults; }
            public void setInitialResults(Integer initialResults) { this.initialResults = initialResults; }
        }
        public Search getSearch() { return search; }
        public void setSearch(Search search) { this.search = search; }
    }

    public static class Bm25 {
        private Index index = new Index();
        public static class Index {
            private String directory;
            private Double k1 = 1.2;
            private Double b = 0.75;
            public String getDirectory() { return directory; }
            public void setDirectory(String directory) { this.directory = directory; }
            public Double getK1() { return k1; }
            public void setK1(Double k1) { this.k1 = k1; }
            public Double getB() { return b; }
            public void setB(Double b) { this.b = b; }
        }
        public Index getIndex() { return index; }
        public void setIndex(Index index) { this.index = index; }
    }

    public static class Rag {
        private Chunk chunk = new Chunk();
        public static class Chunk {
            private Integer size = 500;
            private Integer overlap = 100;
            public Integer getSize() { return size; }
            public void setSize(Integer size) { this.size = size; }
            public Integer getOverlap() { return overlap; }
            public void setOverlap(Integer overlap) { this.overlap = overlap; }
        }
        public Chunk getChunk() { return chunk; }
        public void setChunk(Chunk chunk) { this.chunk = chunk; }
    }

    public static class Storage {
        private String uploadDir;
        public String getUploadDir() { return uploadDir; }
        public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
    }

    public static class Vectorstore {
        private Persistence persistence = new Persistence();
        public static class Persistence {
            private Boolean enabled = true;
            private String dataDir;
            private Boolean autoSave = true;
            private Boolean autoLoad = true;
            public Boolean getEnabled() { return enabled; }
            public void setEnabled(Boolean enabled) { this.enabled = enabled; }
            public String getDataDir() { return dataDir; }
            public void setDataDir(String dataDir) { this.dataDir = dataDir; }
            public Boolean getAutoSave() { return autoSave; }
            public void setAutoSave(Boolean autoSave) { this.autoSave = autoSave; }
            public Boolean getAutoLoad() { return autoLoad; }
            public void setAutoLoad(Boolean autoLoad) { this.autoLoad = autoLoad; }
        }
        public Persistence getPersistence() { return persistence; }
        public void setPersistence(Persistence persistence) { this.persistence = persistence; }
    }

    public static class Regulation {
        private List<String> types;
        public List<String> getTypes() { return types; }
        public void setTypes(List<String> types) { this.types = types; }
    }

    // Getters and Setters for top level fields
    public Gemini getGemini() { return gemini; }
    public void setGemini(Gemini gemini) { this.gemini = gemini; }
    public Cohere getCohere() { return cohere; }
    public void setCohere(Cohere cohere) { this.cohere = cohere; }
    public Embedding getEmbedding() { return embedding; }
    public void setEmbedding(Embedding embedding) { this.embedding = embedding; }
    public Vector getVector() { return vector; }
    public void setVector(Vector vector) { this.vector = vector; }
    public Reranking getReranking() { return reranking; }
    public void setReranking(Reranking reranking) { this.reranking = reranking; }
    public Hybrid getHybrid() { return hybrid; }
    public void setHybrid(Hybrid hybrid) { this.hybrid = hybrid; }
    public Bm25 getBm25() { return bm25; }
    public void setBm25(Bm25 bm25) { this.bm25 = bm25; }
    public Rag getRag() { return rag; }
    public void setRag(Rag rag) { this.rag = rag; }
    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }
    public Vectorstore getVectorstore() { return vectorstore; }
    public void setVectorstore(Vectorstore vectorstore) { this.vectorstore = vectorstore; }
    public Regulation getRegulation() { return regulation; }
    public void setRegulation(Regulation regulation) { this.regulation = regulation; }
    public Mail getMail() { return mail; }
    public void setMail(Mail mail) { this.mail = mail; }
}


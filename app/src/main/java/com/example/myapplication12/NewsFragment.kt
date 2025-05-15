package com.example.myapplication12

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication12.databinding.FragmentNewsBinding
import java.text.SimpleDateFormat
import java.util.*

data class NewsArticle(
    val title: String,
    val source: String,
    val description: String,
    val date: String
)

class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadSampleArticles()
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter()
        binding.newsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@NewsFragment.adapter
        }
    }

    private fun loadSampleArticles() {
        val articles = listOf(
            NewsArticle(
                "Federal Reserve Signals Potential Rate Cuts in 2024",
                "Financial Times",
                "The Federal Reserve has indicated that it may begin cutting interest rates in 2024 as inflation shows signs of cooling. This could have significant implications for mortgage rates and investment strategies.",
                "2024-03-15"
            ),
            NewsArticle(
                "Tech Stocks Rally as AI Boom Continues",
                "Wall Street Journal",
                "Technology stocks are experiencing a strong rally as artificial intelligence companies report record-breaking earnings. Major tech firms are investing heavily in AI infrastructure.",
                "2024-03-14"
            ),
            NewsArticle(
                "Global Markets React to Economic Data",
                "Bloomberg",
                "Global markets showed mixed reactions to the latest economic data, with Asian markets gaining while European markets remained cautious ahead of key policy decisions.",
                "2024-03-13"
            ),
            NewsArticle(
                "Cryptocurrency Market Shows Signs of Recovery",
                "CoinDesk",
                "Bitcoin and other major cryptocurrencies have shown strong recovery signs, with trading volumes increasing and institutional adoption continuing to grow.",
                "2024-03-12"
            ),
            NewsArticle(
                "Housing Market Update: Prices Stabilize",
                "CNBC",
                "The housing market shows signs of stabilization as mortgage rates level off and inventory begins to increase in major metropolitan areas.",
                "2024-03-11"
            ),
            NewsArticle(
                "Renewable Energy Stocks Surge",
                "Reuters",
                "Shares of renewable energy companies have surged following new government incentives and increased demand for clean energy solutions.",
                "2024-03-10"
            ),
            NewsArticle(
                "Banking Sector Reports Strong Q1 Earnings",
                "Financial Times",
                "Major banks have reported stronger-than-expected first-quarter earnings, driven by increased lending activity and improved market conditions.",
                "2024-03-09"
            ),
            NewsArticle(
                "Oil Prices Fluctuate Amid Supply Concerns",
                "Bloomberg",
                "Crude oil prices have shown significant volatility as geopolitical tensions and supply chain disruptions continue to impact global markets.",
                "2024-03-08"
            )
        )
        adapter.submitList(articles)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class NewsAdapter : androidx.recyclerview.widget.ListAdapter<NewsArticle, NewsAdapter.ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ArticleViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        private val title: android.widget.TextView = view.findViewById(R.id.article_title)
        private val source: android.widget.TextView = view.findViewById(R.id.article_source)
        private val description: android.widget.TextView = view.findViewById(R.id.article_description)
        private val date: android.widget.TextView = view.findViewById(R.id.article_date)

        fun bind(article: NewsArticle) {
            title.text = article.title
            source.text = article.source
            description.text = article.description
            date.text = article.date
        }
    }
}

class ArticleDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<NewsArticle>() {
    override fun areItemsTheSame(oldItem: NewsArticle, newItem: NewsArticle): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: NewsArticle, newItem: NewsArticle): Boolean {
        return oldItem == newItem
    }
} 
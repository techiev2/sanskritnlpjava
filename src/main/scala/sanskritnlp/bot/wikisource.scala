package sanskritnlp.bot

import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot
import org.slf4j.LoggerFactory
import sanskritnlp.ocr.GocrOutputIterator
import sanskritnlp.transliteration.optitrans

/**
  * Created by vvasuki on 2/28/16.
  */
object wikisource extends wikiBot {
  override val languageCode = "sa"
  override val wikiSiteName = "wikisource"
  override val log = LoggerFactory.getLogger(this.getClass)

  def getIndexPage(fileTitle: String) = {
    var article = bot.readData(s"index:$fileTitle")
    log info article.getRevisionId.toString
    log info article.getText
    // TODO: Do more here?
  }
  def getIndexedPageTitle(fileTitle: String, pageNum: Int, numberLanguage: String = ""): String = {
    var pageString = pageNum.toString
    if (numberLanguage == "sa") {
      pageString = optitrans.toDevanagari(pageString).get
    }
    return s"page:$fileTitle/$pageString"
  }

  def getIndexedPage(fileTitle: String, pageNum: Int, numberLanguage: String = "") = {
    var article = bot.readData(getIndexedPageTitle(fileTitle, pageNum, numberLanguage))
    log info fileTitle
    log info article.getRevisionId.toString
    log info article.getText
    // TODO: Do more here?
  }

  def getIndexedPages(fileTitle: String, startPage: Int, endPage: Int, numberLanguage: String = "") = {
    Range(startPage, endPage+1).foreach(pageNum => {
      getIndexedPage(fileTitle = fileTitle, pageNum = pageNum, numberLanguage = numberLanguage)
    })
  }

  def indexPageTests = {
    // getIndexPage("मेघसन्देशः - दक्षिणावर्तनाथः - १९१९.djvu")

    // Yields empty string for text and revision.
    // getIndexedPage("मेघसन्देशः - दक्षिणावर्तनाथः - १९१९.djvu", 100)
    // getIndexedPage("Ganaratnamahodadhi.pdf", 10)
    // log info getIndexedPageTitle("ADictionaryOfSanskritGrammarByMahamahopadhyayaKashinathVasudevAbhyankar.djvu", 432, numberLanguage = "sa")
    getIndexedPages("ADictionaryOfSanskritGrammarByMahamahopadhyayaKashinathVasudevAbhyankar.djvu", 20, 30, numberLanguage = "sa")
  }

  // Bot approval request: https://en.wikipedia.org/wiki/Wikipedia:Bots/Requests_for_approval/sanskritnlpbot
  def fillIndexedPages(ocrOutput: GocrOutputIterator, startPage: Int, endPage: Int,
                       numberLanguage: String = "", fileTitle: String, overwrite: Boolean = false, dryRun: Boolean = true): Unit = {
    ocrOutput.skipNPages(startPage - 1)
    Range(startPage, endPage+1).foreach(pageNum => {
      val title = getIndexedPageTitle(fileTitle = fileTitle, pageNum = pageNum, numberLanguage = numberLanguage)
      var write = false
      if (overwrite) {
        write = true
      }
      log info write.toString
      if (write && ocrOutput.hasNext) {
        val pageText = ocrOutput.nextPage
        val wikiText =
          s"""
             <poem>
             $pageText
             </poem>
           """
        /* TODO: Cannot write noinclude stuff like:
                     <noinclude>
             <pagequality level="2" user="$userName" />
             <div class="pagetext">
             </noinclude>
         */
        log info title
        log info wikiText
        if (!dryRun) {
          edit(title = title, text = wikiText, summary = "Fill OCR data.")
        }
      }
    })
  }

  def fillIndexedPagesTest = {
    val gocrOut = new GocrOutputIterator("/home/vvasuki/sanskrit-ocr-r0/vaak/vyAkaraNam/abhyankar-grammar/abhyankar-grammar-gocr.txt")
    fillIndexedPages(gocrOut, startPage = 26, endPage = 36, numberLanguage = "sa",
      fileTitle = "ADictionaryOfSanskritGrammarByMahamahopadhyayaKashinathVasudevAbhyankar.djvu", overwrite = true, dryRun = false)
  }

  def main(args: Array[String]): Unit = {
    passwd = ""
    login
    // test
    // indexPageTests
    fillIndexedPagesTest
  }
}

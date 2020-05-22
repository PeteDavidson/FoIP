#!/usr/bin/env python
################################################################################
#
#   T38 Trace Log Parser
#
#   Copyright 2015 Lexmark International, Inc.
#   All Rights Reserved. Proprietary and Confidential.
#
#   mcarrill@lexmark.com
#   2015-12-11
#
################################################################################

import sys
import getopt
import os
import re

Options = {'verbose'    : 0,
           'parse-sip'  : 'yes',
           'parse-t38'  : 'yes',
           'parse-at'   : 'no',
           'parse-h323' : 'no'}

class ParserUtils:

    def __init(self):
        pass

    def parse_date_time(self, line):
        date_time = ""
        m = re.search("^[0-9].../[0-9]./[0-9].", line)
        if m:
            line = line.replace("\t", " ")
            tokens = line.split(" ")
            date_time = "%s %s" % (tokens[0], tokens[1])
        return date_time

    def parse_ip_address(self, line):
        ip = ""
        tmp = line.split(" ")
        ip += "%d." % int(tmp[0], 16)
        ip += "%d." % int(tmp[1], 16)
        ip += "%d." % int(tmp[2], 16)
        ip += "%d" % int(tmp[3], 16)
        return ip

    def print_no_eol(self, string):
        sys.stdout.write(string)
        sys.stdout.flush()

class DataParser:

    def __init__(self, keyword1, keyword2):
        self._keyword1 = keyword1
        self._keyword2 = keyword2
        self._parsing = 0
        self._first_line = 0
        self._temp_data = ""
        self.data = ""
        pass

    def clear(self):
        self._parsing = 0
        self._first_line = 0
        self._temp_data = ""
        self.data = ""

    def parse(self, line):
        found_start = 0
        found_end = 0
        if len(self._temp_data) > 0:
            self._parsing = 1
            self._first_line = 1
            self._save_line(self._temp_data)
            self._temp_data = ""
        if self._parsing == 0:
            found_start = self._find_start(line)
            if found_start == 1:
                self._parsing = 1
                self._first_line = 1
        if self._parsing == 1 and self._first_line == 0:
            found_end = self._find_end(line)
        if found_end == 0 and self._parsing == 1:
            self._save_line(line)
        else:
            found_start = self._find_start(line)
            if found_start == 1:
                self._temp_data = line
        return found_end

    def _find_start(self, line):
        found = 0
        m = re.search(self._keyword1, line)
        if m:
            if self._keyword2 != "":
                m = re.search(self._keyword2, line)
                if m:
                    found = 1
            else:
                found = 1
        return found

    def _find_end(self, line):
        found = 0
        m = Utils.parse_date_time(line)
        if len(m) > 0:
            self._parsing = 0
            found = 1
        return found

    def _save_line(self, line):
        if self._first_line == 1:
            self.data = line
            self._first_line = 0
        else:
            self.data = self.data + line

class FileSeparatorParser:

    def __init__(self):
        self._data = ""

    def parse(self, line):
        found = 0
        line = line.strip()
        m = re.search("^=====* T.38 Trace", line)
        if m:
            found = 1
        else:
            m = re.search("^=====* T38 Trace", line)
            if m:
                found = 1
        if found:
            self._data = line
        return found

    def show(self):
        if len(self._data) > 0:
            print self._data;
            self._data = ""

class T38ModemVersionParser:

    def __init__(self):
        self.data = ""
        self.found = 0
        self._parser = DataParser("T38Modem", "T38Modem Version")

    def parse(self, line, mode):
        end = self._parser.parse(line)
        if end == 1:
            if mode == "summary":
                self._show_summary(self._parser.data)
            else:
                self._show_detailed(self._parser.data)
            self.found = 1

    def parse_when_trace_level_0(self, line):
        line = line.strip()
        if len(self.data) == 0:
            m = re.search("^T38Modem Version", line)
            if m:
                self.data = line
        else:
            self.data += line
            self.data = self.data.replace("Version", "Version:")
            self.found = 1
        if self.found:
            print self.data
            self.data = ""

    def clear(self):
        self._parser.clear()
        self.data = ""
        self.found = 0

    def _show_detailed(self, data):
        Utils.print_no_eol(data)

    def _show_summary(self, data):
        version = ""
        date_time = Utils.parse_date_time(data)
        m = re.search("(T38Modem Version .*)", data)
        if m:
            version = m.group(1)
        summary = "%s ----- %s" % (date_time, version)
        print summary

class T38ModemOptionsParser:

    def __init__(self):
        self.data = ""
        self.found = 0
        self._parser = DataParser("T38Modem", "Options")

    def parse(self, line, mode):
        end = self._parser.parse(line)
        if end == 1:
            if mode == "summary":
                self._show_summary(self._parser.data)
            else:
                self._show_detailed(self._parser.data)
            self.found = 1

    def parse_when_trace_level_0(self, line):
        line = line.strip()
        if len(self.data) == 0:
            m = re.search("/pkg-fax/bin/t38modem", line)
            if m:
                self.data = "T38Modem Options: %s" % line
            else:
                m = re.search("/usr/bin/t38modem", line)
                if m:
                    self.data = "T38Modem Options: %s" % line
        else:
            if len(line) > 0:
                self.data += "%s" % line
            else:
                self.found = 1
        if self.found == 1:
            print "%s" % self.data
            self.data = ""

    def clear(self):
        self._parser.clear()
        self.data = ""
        self.found = 0

    def _show_detailed(self, data):
        Utils.print_no_eol(data)

    def _show_summary(self, data):
        data = data.replace("\n", "")
        options = ""
        date_time = Utils.parse_date_time(data)
        m = re.search("Options: (.*)", data)
        if m:
            options = m.group(1)
        summary = "%s ----- T38Modem Options %s" % (date_time, options)
        print summary

class SIPSummary:

    def __init__(self):
        self.ip         = ""
        self.port       = ""
        self._direction = ""
        self._date_time = ""
        self._remote_ip = ""
        self._local_ip  = ""
        self._sip       = ""
        self._user      = ""
        self._cseq      = ""

    def create(self, data, direction):
        self._direction = direction
        lines = data.split("\n")
        for line in lines:
            self._find_date_time(line)
            self._find_remote_ip(line)
            self._find_local_ip(line)
            self._find_sip(line)
            self._find_cseq(line)
            self._find_ip(line)
            self._find_port(line)
        print self._create_summary_line()

    def _find_date_time(self, line):
        if self._date_time == "":
            self._date_time = Utils.parse_date_time(line)

    def _find_remote_ip(self, line):
        if self._remote_ip == "":
            m = re.search("rem=(.*),local", line)
            if m:
                self._remote_ip = m.group(1)

    def _find_local_ip(self, line):
        if self._local_ip == "":
            m = re.search("local=(.*),if", line)
            if m:
                self._local_ip = m.group(1)

    def _find_sip(self, line):
        if self._sip == "":
            if line.find("SIP/2.0") == 0:
                line = line.replace("SIP/2.0 ", "")
                self._sip = line
            elif line.endswith("SIP/2.0"):
                line = line.replace(" SIP/2.0", "")
                tokens = line.split(" ")
                self._sip = tokens[0]
            if self._sip != "":
                m = re.search("(sip:[0-9]*)@", line)
                if m:
                    self._user = m.group(1)

    def _find_cseq(self, line):
        if self._cseq == "":
            if line.find("CSeq:") != -1:
                self._cseq = line

    def _find_ip(self, line):
        if self.ip == "":
            parse = 0
            if self._sip == "INVITE":
                parse = 1
            if self._sip == "200 OK" and self._cseq.find("INVITE") != -1:
                parse = 1
            if parse == 1:
                m = re.search("c=IN IP4 (.*)", line)
                if m:
                    self.ip = m.group(1)

    def _find_port(self, line):
        if self.port == "":
            parse = 0
            if self._sip == "INVITE":
                parse = 1
            if self._sip == "200 OK" and self._cseq.find("INVITE") != -1:
                parse = 1
            if parse == 1:
                m = re.search("^m=([a-z]*) ([0-9]*)", line)
                if m:
                    self.port = m.group(2)

    def _create_summary_line(self):
        self._sip = "SIP : %s %s" % (self._sip, self._user)
        summary = "%s %s %-30.30s %-22.22s | %s %s %s" % (
                    self._date_time,
                    self._direction,
                    self._sip,
                    self._cseq,
                    self._local_ip,
                    self._direction,
                    self._remote_ip)
        return summary

class SIPOutgoingParser:

    def __init__(self):
        self._direction = ">>>>>"
        self._parser = DataParser("SIP", "Sending PDU")

    def parse(self, line, mode, ip):
        self._ip = ip
        end = self._parser.parse(line)
        if end == 1:
            if mode == "summary":
                self._show_summary(self._parser.data)
            else:
                self._show_detailed(self._parser.data)
        return self._ip

    def _show_detailed(self, data):
        Utils.print_no_eol(data)

    def _show_summary(self, data):
        summary = SIPSummary()
        summary.create(data, self._direction)
        if summary.ip != "":
            self._ip["local_ip"] = summary.ip
        if summary.port != "":
            self._ip["local_port"] = summary.port

class SIPIncomingParser:

    def __init__(self):
        self._direction = "<<<<<"
        self._parser = DataParser("SIP", "PDU received")

    def parse(self, line, mode, ip):
        self._ip = ip
        end = self._parser.parse(line)
        if end == 1:
            if mode == "summary":
                self._show_summary(self._parser.data)
            else:
                self._show_detailed(self._parser.data)
        return self._ip

    def _show_detailed(self, data):
        Utils.print_no_eol(data)

    def _show_summary(self, data):
        summary = SIPSummary()
        summary.create(data, self._direction)
        if summary.ip != "":
            self._ip["remote_ip"] = summary.ip
        if summary.port != "":
            self._ip["remote_port"] = summary.port

class T38Summary:

    def __init__(self):
        self._direction = ""
        self._date_time = ""
        self._msg_type = ""
        self._field_type = ""
        self._field_size = 0
        self._field_data = 0
        self._field_data_rev  = 0
        self._field_data_char = ""
        self._frames = {
            0x80 : "CALLING_UNIT",
            0x00 : "CALLED_UNIT",
            0x01 : "DIS",
            0x81 : "DTC",
            0x41 : "DCS",
            0x02 : "CSI",
            0x82 : "CIG",
            0x42 : "TSI",
            0x04 : "NSF",
            0x84 : "NSC",
            0x44 : "NSS",
            0x58 : "CRP",
            0x21 : "CFR",
            0x22 : "FTT",
            0x71 : "EOM",
            0x72 : "MPS",
            0x74 : "EOP",
            0x31 : "MCF",
            0x33 : "RTP",
            0x32 : "RTN",
            0x48 : "CTC",
            0x23 : "CTR",
            0x5F : "DCN",
            0x35 : "PIP",
            0x34 : "PIN",
            0x79 : "PRI_EOM",
            0x7A : "PRI_MPS",
            0x7C : "PRI_EOP",
            0x83 : "PWD_DTC",
            0x45 : "PWD_DCS",
            0x85 : "SEP",
            0x43 : "SUB",
            0x7D : "PPS",
            0x3D : "PPR",
            0x48 : "CTC",
            0x73 : "EOR",
            0x76 : "RR",
            0x37 : "RNR",
            0x38 : "ERR",
        }

    def create(self, data, direction, ip, frames_info):
        self._ip = ip
        self._frames_info = frames_info
        self._direction = direction
        lines = data.split("\n")
        for line in lines:
            self._find_date_time(line)
            self._find_msg_type(line)
            self._find_field_type(line)
            self._find_field_size(line)
            self._find_field_data(line)
        self._process_data()
        print self._create_summary_line()
        return self._frames_info

    def _find_date_time(self, line):
        if self._date_time == "":
            self._date_time = Utils.parse_date_time(line)

    def _find_msg_type(self, line):
        if self._msg_type == "":
            m = re.search("type_of_msg = (.*)", line)
            if m:
                self._msg_type = m.group(1)

    def _find_field_type(self, line):
        if self._field_type == "":
            m = re.search("field_type = (.*)", line)
            if m:
                self._field_type = m.group(1)
                if self._field_type == "hdlc-data":
                    self._frames_info["byte_number"] += 1
                elif self._field_type == "hdlc-fcs-OK":
                    self._frames_info["byte_number"] = 0

    def _find_field_size(self, line):
        if self._field_size == 0:
            m = re.search("field_data = (.*) octets", line)
            if m:
                token = m.group(1)
                self._field_size = int(token.replace(" ", ""))

    def _find_field_data(self, line):
        if self._field_size == 1 and self._field_data == 0:
            m = re.search("( *)([0-9abcdef][0-9abcdef]) ", line)
            if m:
                token = m.group(2)
                self._field_data = int(token, 16)

    def _process_data(self):
        if self._field_size == 1:
            if self._field_type == "hdlc-data":
                self._field_data_rev = self._reverse_bits_of_byte(self._field_data)
                self._field_data_char = self._get_printable_char(self._field_data_rev)
                if self._frames_info["byte_number"] == 3:
                    self._frames_info["frame_name"] = self._get_hdlc_frame_name(self._field_data)
                if self._frames_info["byte_number"] == 4 and self._frames_info["frame_name"] == "PPS":
                    self._frames_info["frame_name_2"] = self._get_hdlc_frame_name(self._field_data)
                if self._frames_info["byte_number"] > 3:
                    if self._frames_info["frame_name"] == "TSI" or self._frames_info["frame_name"] == "CSI":
                        self._frames_info["station_id"] = self._frames_info["station_id"] + self._field_data_char

    def _reverse_bits_of_byte(self, byte):
        tmp = bin(byte)
        tmp = tmp.replace("0b", "")
        tmp = tmp.zfill(8)
        tmp = tmp[::-1]
        return int(tmp, 2)

    def _get_printable_char(self, num):
        if 31 < num and num < 127:
            char = chr(num)
        else:
            char = "."
        return char

    def _get_hdlc_frame_name(self, num):
        num = num & 0x7F
        name = "%X" % num
        for key in self._frames.keys():
            if key == num:
                name = self._frames[key]
        return name

    def _create_summary_line_t38_data_0(self):
        field_type = self._field_type
        if field_type == "hdlc-fcs-OK":
            if self._frames_info["frame_name_2"] != "":
                field_type = "%s %s-%s" % (field_type, self._frames_info["frame_name"], self._frames_info["frame_name_2"])
            else:
                field_type = "%s %s" % (field_type, self._frames_info["frame_name"])
            self._frames_info["frame_name"] = ""
            self._frames_info["frame_name_2"] = ""
        if self._frames_info["station_id"] != "":
            field_type = "%s (%s)" % (field_type, self._frames_info["station_id"][::-1])
            self._frames_info["station_id"] = ""
        t38 = "T38 : %s %s" % (self._msg_type, field_type)
        return t38

    def _create_summary_line_t38_data_1(self):
        frame_name = ""
        if self._frames_info["byte_number"] == 3 and self._frames_info["frame_name"] != "":
            frame_name = self._frames_info["frame_name"]
        if self._frames_info["byte_number"] == 4 and self._frames_info["frame_name_2"] != "":
            frame_name = self._frames_info["frame_name_2"]
        t38 = "T38 : %s %s - %02X (%02X) (%s) %s" % (
                    self._msg_type,
                    self._field_type,
                    self._field_data,
                    self._field_data_rev,
                    self._field_data_char,
                    frame_name)
        return t38

    def _create_summary_line_t38_data_2(self):
        t38 = "T38 : %s %s - %d bytes" % (
                    self._msg_type,
                    self._field_type,
                    self._field_size)
        return t38

    def _create_summary_line(self):
        if self._field_size == 0:
            t38 = self._create_summary_line_t38_data_0()
        elif self._field_size == 1:
            t38 = self._create_summary_line_t38_data_1()
        else:
            t38 = self._create_summary_line_t38_data_2()
        if self._ip["local_ip"] == "":
            summary = "%s %s %-53.53s" % (
                        self._date_time,
                        self._direction,
                        t38)
        else:
            summary = "%s %s %-53.53s | udp$%s:%s %s udp$%s:%s" % (
                        self._date_time,
                        self._direction,
                        t38,
                        self._ip["local_ip"],
                        self._ip["local_port"],
                        self._direction,
                        self._ip["remote_ip"],
                        self._ip["remote_port"])
        return summary

class T38OutgoingParser:

    def __init__(self):
        self._direction = ">>>>>"
        self._parser = DataParser("T38ModemMediaStream::ReadPacket ifp", "");
        self._frames_info = {
            "byte_number"  : 0,
            "frame_name"   : "",
            "frame_name_2" : "",
            "station_id"   : "",
        }

    def parse(self, line, mode, ip):
        self._ip = ip
        end = self._parser.parse(line)
        if end == 1:
            if mode == "summary":
                self._show_summary(self._parser.data)
            else:
                self._show_detailed(self._parser.data)

    def _show_detailed(self, data):
        Utils.print_no_eol(data)

    def _show_summary(self, data):
        summary = T38Summary()
        self._frames_info = summary.create(data, self._direction, self._ip, self._frames_info)

class T38IncomingParser:

    def __init__(self):
        self._direction = "<<<<<"
        self._parser = DataParser("T38Engine HandlePacket Received ifp", "");
        self._frames_info = {
            "byte_number"  : 0,
            "frame_name"   : "",
            "frame_name_2" : "",
            "station_id"   : "",
        }

    def parse(self, line, mode, ip):
        self._ip = ip
        end = self._parser.parse(line)
        if end == 1:
            if mode == "summary":
                self._show_summary(self._parser.data)
            else:
                self._show_detailed(self._parser.data)

    def _show_detailed(self, data):
        Utils.print_no_eol(data)

    def _show_summary(self, data):
        summary = T38Summary()
        self._frames_info = summary.create(data, self._direction, self._ip, self._frames_info)

class ATSummary:

    def __init__(self):
        self._direction  = ""
        self._date_time  = ""
        self._at         = ""
        self._cmd        = ""
        self._data_ascii = ""
        self._data_hex   = ""

    def create(self, data, direction):
        self._direction = direction
        lines = data.split("\n")
        for line in lines:
            self._find_date_time(line)
            self._find_cmd(line)
            self._find_data(line)
        print self._create_summary_line()

    def _find_date_time(self, line):
        if self._date_time == "":
            self._date_time = Utils.parse_date_time(line)

    def _find_cmd(self, line):
        if self._cmd == "":
            if self._direction == ">>>>>":
                m = re.search("--> (.*)", line)
                if m:
                    self._cmd = m.group(1)
            else:
                m = re.search("<-- (.*) {$", line)
                if m:
                    token = "%s" % m.group(1)
                    self._cmd = token.strip()
                else:
                    m = re.search("<-- (.*)$", line)
                    if m:
                        token = "%s" % m.group(1)
                        self._cmd = token.strip()

    def _find_data(self, line):
        m = re.search("^  (.*)   (.*)$", line)
        if m:
            data_hex = "%s" % m.group(1)
            data_hex = data_hex.upper()
            data_hex = data_hex.replace("  ", " ")
            data_ascii = "%s" % m.group(2)
            data_ascii = data_ascii.replace("}", "")
            self._data_hex += "%s " % data_hex.strip()
            self._data_ascii += "%s" % data_ascii.strip()

    def _create_summary_line(self):
        if self._direction == ">>>>>":
            self._at = "AT  : %s" % self._cmd
        else:
            if self._cmd == "":
                self._at = "AT  :    %s" % self._data_ascii
            else:
                self._at = "AT  :    %s : %s" % (self._cmd, self._data_ascii)
        summary = "%s %s %-53.53s | %s " % (
                    self._date_time,
                    self._direction,
                    self._at,
                    self._data_hex)
        return summary

class ATOutgoingParser:

    def __init__(self):
        self._direction = ">>>>>"
        self._parser = DataParser("ttyT38mode", "-->")

    def parse(self, line, mode):
        end = self._parser.parse(line)
        if end == 1:
            if mode == "summary":
                self._show_summary(self._parser.data)
            else:
                self._show_detailed(self._parser.data)

    def _show_detailed(self,data):
        Utils.print_no_eol(data)

    def _show_summary(self,data):
        summary = ATSummary()
        summary.create(data, self._direction)

class ATIncomingParser:

    def __init__(self):
        self._direction = "<<<<<"
        self._parser = DataParser("ttyT38mode", "<--")

    def parse(self, line, mode):
        end = self._parser.parse(line)
        if end == 1:
            if mode == "summary":
                self._show_summary(self._parser.data)
            else:
                self._show_detailed(self._parser.data)

    def _show_detailed(self,data):
        Utils.print_no_eol(data)

    def _show_summary(self,data):
        summary = ATSummary()
        summary.create(data, self._direction)

class H323Summary:

    def __init__(self):
        self._direction     = ""
        self._date_time     = ""
        self._request       = ""
        self._response      = ""
        self._command       = ""
        self._msg_type      = ""
        self._h323          = ""
        self._parse_dest_ip = 0
        self._parse_src_ip  = 0
        self._ip            = {}

    def create(self, data, direction, ip):
        self._direction = direction
        self._ip = ip
        lines = data.split("\n")
        for line in lines:
            self._find_date_time(line)
            self._find_request(line)
            self._find_response(line)
            self._find_command(line)
            self._find_msg_type(line)
            self._find_remote_ip(line)
            self._find_local_ip(line)
        print self._create_summary_line()

    def _find_date_time(self, line):
        if self._date_time == "":
            self._date_time = Utils.parse_date_time(line)

    def _find_request(self, line):
        if self._request == "":
            m = re.search("request (.*) {", line)
            if m:
                self._request = m.group(1)

    def _find_response(self, line):
        if self._response == "":
            m = re.search("response (.*) {", line)
            if m:
                self._response = m.group(1)

    def _find_command(self, line):
        if self._command == "":
            m = re.search("command (.*)$", line)
            if m:
                self._command = m.group(1)

    def _find_msg_type(self, line):
        if self._msg_type == "":
            m = re.search("messageType = (.*)", line)
            if m:
                self._msg_type = m.group(1)

    def _find_remote_ip(self, line):
        if self._ip["remote_ip"] == "":
            if self._msg_type == "Setup":
                m = re.search("destCallSignalAddress", line)
                if m:
                    self._parse_dest_ip = 1
        if self._parse_dest_ip == 1:
            m = re.search("([0-9abcdef][0-9abcdef] [0-9abcdef][0-9abcdef] [0-9abcdef][0-9abcdef] [0-9abcdef][0-9abcdef])", line)
            if m:
                self._ip["remote_ip"] = Utils.parse_ip_address(m.group(1))
                self._parse_dest_ip = 0

    def _find_local_ip(self, line):
        if self._ip["local_ip"] == "":
            if self._msg_type == "Setup":
                m = re.search("sourceCallSignalAddress", line)
                if m:
                    self._parse_src_ip = 1
        if self._parse_src_ip == 1:
            m = re.search("([0-9abcdef][0-9abcdef] [0-9abcdef][0-9abcdef] [0-9abcdef][0-9abcdef] [0-9abcdef][0-9abcdef])", line)
            if m:
                self._ip["local_ip"] = Utils.parse_ip_address(m.group(1))
                self._parse_src_ip = 0

    def _create_summary_line(self):
        self._h323 = "H323: "
        if len(self._request) > 0:
            self._h323 += "request = %s" % (self._request)
        elif len(self._response) > 0:
            self._h323 += "response = %s" % (self._response)
        elif len(self._command) > 0:
            self._h323 += "command = %s" % (self._command)
        if len(self._msg_type) > 0:
            self._h323 += "msg_type = %s" % (self._msg_type)
            if self._msg_type == "ReleaseComplete":
                self._ip["remote_ip"] = ""
                self._ip["local_ip"] = ""
        summary = "%s %s %-53.53s | %s %s %s" % (
                    self._date_time,
                    self._direction,
                    self._h323,
                    self._ip["local_ip"],
                    self._direction,
                    self._ip["remote_ip"])
        return summary

class H323OutgoingParser:

    def __init__(self):
        self._direction = ">>>>>"
        self._parser = DataParser("Sending PDU:", "H2[24]5")

    def parse(self, line, mode, ip):
        self._ip = ip
        end = self._parser.parse(line)
        if end == 1:
            if mode == "summary":
                self._show_summary(self._parser.data)
            else:
                self._show_detailed(self._parser.data)
        return self._ip

    def _show_detailed(self, data):
        Utils.print_no_eol(data)

    def _show_summary(self, data):
        summary = H323Summary()
        summary.create(data, self._direction, self._ip)
        self._ip = summary._ip

class H323IncomingParser:

    def __init__(self):
        self._direction = "<<<<<"
        self._parser = DataParser("Receiving PDU:", "H2[24]5")

    def parse(self, line, mode, ip):
        self._ip = ip
        end = self._parser.parse(line)
        if end == 1:
            if mode == "summary":
                self._show_summary(self._parser.data)
            else:
                self._show_detailed(self._parser.data)
        return self._ip

    def _show_detailed(self, data):
        Utils.print_no_eol(data)

    def _show_summary(self, data):
        summary = H323Summary()
        summary.create(data, self._direction, self._ip)
        self._ip = summary._ip

class T38TraceParser:

    def __init__(self):
        self._file_separator   = FileSeparatorParser()
        self._t38modem_version = T38ModemVersionParser()
        self._t38modem_options = T38ModemOptionsParser()
        self._sip_outgoing     = SIPOutgoingParser()
        self._sip_incoming     = SIPIncomingParser()
        self._t38_outgoing     = T38OutgoingParser()
        self._t38_incoming     = T38IncomingParser()
        self._at_outgoing      = ATOutgoingParser()
        self._at_incoming      = ATIncomingParser()
        self._h323_outgoing    = H323OutgoingParser()
        self._h323_incoming    = H323IncomingParser()
        self._ip = {
            "remote_ip"   : "",
            "remote_port" : "",
            "local_ip"    : "",
            "local_port"  : "",
        }

    def parse(self, filepath, mode):
        f = open(filepath, "r");
        if f:
            line = f.readline()
            while line:
                if self._file_separator.parse(line) == 1:
                    self._t38modem_version.clear()
                    self._t38modem_options.clear()
                    # Set line data to date/time string to force all active parsing
                    # to end and display data.
                    line = "2016/01/01\t00:00:00"
                if self._t38modem_version.found == 0:
                    self._t38modem_version.parse(line, mode)
                    if self._t38modem_version.found == 0:
                        self._t38modem_version.parse_when_trace_level_0(line)
                if self._t38modem_options.found == 0:
                    self._t38modem_options.parse(line, mode)
                    if self._t38modem_options.found == 0:
                        self._t38modem_options.parse_when_trace_level_0(line)
                if Options['parse-sip'] == 'yes':
                    self._ip = self._sip_outgoing.parse(line, mode, self._ip)
                    self._ip = self._sip_incoming.parse(line, mode, self._ip)
                if Options['parse-t38'] == 'yes':
                    self._t38_outgoing.parse(line, mode, self._ip)
                    self._t38_incoming.parse(line, mode, self._ip)
                if Options['parse-at'] == 'yes':
                    self._at_outgoing.parse(line, mode)
                    self._at_incoming.parse(line, mode)
                if Options['parse-h323'] == 'yes':
                    self._ip = self._h323_outgoing.parse(line, mode, self._ip)
                    self._ip = self._h323_incoming.parse(line, mode, self._ip)
                self._file_separator.show()
                line = f.readline()
            if len(self._t38modem_version.data) > 0:
                print self._t38modem_version.data
            if len(self._t38modem_options.data) > 0:
                print self._t38modem_options.data
        else:
            print("Cannot open file")

class CLIHandler():
    _commands = {}

    def __init__(self):
        self._commands.update({'help':     [ self._show_help,              1 ]})
        self._commands.update({'summary':  [ self._create_summary_report,  1 ]})
        self._commands.update({'detailed': [ self._create_detailed_report, 1 ]})

    def show_usage(self):
        """
NAME
    %s - Parse SIP/T38/H323/AT data from T38 trace file.

SYNOPSIS
    %s [options] <commands> [arguments]

DESCRIPTION
    This program parses SIP packets, T38 packets, H323 packets, and AT traffic
    data from a T38 trace file. The T38 trace file is a log file created by the
    t38modem application.

COMMANDS
%s
OPTIONS
    --verbose
        Verbose output

VERSION
    0.30

CONTACT
    mcarrill@lexmark.com
    """

        commands = ""
        for command in self._commands.keys():
            commands += "    - %s\n" % command
        out = self.show_usage.__doc__ % (Filename, Filename, commands)
        print(out)

    def _get_command_info(self, command):
        name = None
        info = []
        if command in self._commands.keys():
            name = command
            info = self._commands[command]
        return (name, info)

    def get_opts_args(self):
        try:
            longopts = []
            for key, value in Options.iteritems():
                if isinstance(value, str) == 1:
                    key += "="
                    longopts.append(key)
                else:
                    longopts.append(key)
            opts, args = getopt.getopt(sys.argv[1:], "", longopts)
        except getopt.error, msg:
            print msg
            sys.exit(2)
        for opt, arg in opts:
            for key, value in Options.iteritems():
                tmp = "--%s" % key
                if opt == tmp:
                    if isinstance(value, str) == 1:
                        Options[key] = arg
                    else:
                        Options[key] = 1
        return args

    def process_args(self, args):
        command = args[0]
        name, info = self._get_command_info(command)
        if name != None:
            handler = info[0]
            arg_num = info[1]
            if len(args[1:]) >= arg_num:
                handler(args[1:])
            else:
                print("ERROR: Missing arguments for command '%s'" % name)
                self._show_help([name, ""])
        else:
            print("ERROR: Unknown command %s" % command)

    def _show_help(self, args):
        """
SYNOPSIS
    %s help <command>

DESCRIPTION
    Show help for command.
        """
        command = args[0]
        name, info = self._get_command_info(command)
        if name != None:
            handler = info[0]
            print(handler.__doc__ % Filename)
        else:
            print("ERROR: Unknown command %s" % command)

    def _create_summary_report(self, args):
        """
SYNOPSIS
    %s [options] summary <filepath>

DESCRIPTION
    Create summary of parsed data.

    By default, only SIP and T38 parsing are enabled. You can control what
    parsing to enable/disable using the --parse-XXX options. Please see OPTIONS
    section for more details

OPTIONS
    --parse-sip=[yes | no]
        Enable/disable parsing of SIP packets

    --parse-t38=[yes | no]
        Enable parsing of T38 packets

    --parse-at=[yes | no]
        Enable parsing of AT traffic

    --parse-h323=[yes | no]
        Enable parsing of H323 packets
        """
        Parser.parse(args[0], "summary")

    def _create_detailed_report(self, args):
        """
SYNOPSIS
    %s [options] detailed <filepath>

DESCRIPTION
    Create detailed report of parsed data

    By default, only SIP and T38 parsing are enabled. You can control what
    parsing to enable/disable using the --parse-XXX options. Please see OPTIONS
    section for more details

OPTIONS
    --parse-sip=[yes | no]
        Enable/disable parsing of SIP packets

    --parse-t38=[yes | no]
        Enable parsing of T38 packets

    --parse-at=[yes | no]
        Enable parsing of AT traffic

    --parse-h323=[yes | no]
        Enable parsing of H323 packets
        """
        Parser.parse(args[0], "detailed")

Filename = os.path.basename(__file__)
CLI = CLIHandler()
Parser = T38TraceParser()
Utils = ParserUtils()

def main():
    args = CLI.get_opts_args()
    log("ARGS: %s" % args)
    log("OPTS: %s" % Options)
    if len(args) > 0:
        CLI.process_args(args)
    else:
        print("ERROR: Missing <command>")
        CLI.show_usage()

def log(msg, priority=1):
    if Options['verbose'] >= priority:
        print(msg)

if __name__ == "__main__":
    main()

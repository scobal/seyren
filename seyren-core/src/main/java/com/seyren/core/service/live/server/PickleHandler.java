/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.core.service.live.server;

import static org.python.google.common.collect.ImmutableSet.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.modules.cPickle;
import org.python.modules.struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.seyren.core.service.live.Metric;
import com.seyren.core.service.live.MetricsTask;
import com.seyren.core.service.schedule.CheckRunnerFactory;
import com.seyren.core.store.ChecksStore;

/**
 * See http://graphite.readthedocs.org/en/latest/feeding-carbon.html
 */
public class PickleHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PickleHandler.class);
    private static final String ISO_8859_1 = "ISO-8859-1";

    private Socket socket;
    private Executor executor;
    private ChecksStore checksStore;
    private CheckRunnerFactory checkRunnerFactory;

    public PickleHandler(Socket socket, Executor executor, ChecksStore checksStore, CheckRunnerFactory checkRunnerFactory) {
        this.socket = socket;
        this.executor = executor;
        this.checksStore = checksStore;
        this.checkRunnerFactory = checkRunnerFactory;
    }

    @Override
    public void run() {
        LOGGER.debug("Accepted.");
        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();

            while (true) {
                // http://graphite.readthedocs.org/en/latest/feeding-carbon.html
                // Send the data over a socket to Carbon’s pickle receiver
                // You’ll need to pack your pickled data into a packet containing a simple header:
                //   payload = pickle.dumps(listOfMetricTuples)
                //   header = struct.pack("!L", len(payload))
                //   message = header + payload
                // Here, we have to decode this kind of message
                BigInteger length = getLength(inputStream);
                LOGGER.debug("Pickle length {}", length);

                List<Metric> metrics = getMetrics(inputStream, length);
                LOGGER.debug("Pickle size: {}", metrics.size());
                executor.execute(new MetricsTask(copyOf(metrics), checksStore, checkRunnerFactory));
            }
        } catch (Exception e) {
            LOGGER.warn("An error occurs when decoding pickle message: '{}' (change log level to debug to see stack trace).", e.getMessage());
            LOGGER.debug("An error occurs when decoding pickle message: ", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private BigInteger getLength(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[4];
        inputStream.read(bytes);
        String stHeader = new String(bytes, ISO_8859_1);
        PyTuple tuple = struct.unpack("!L", stHeader);
        return (BigInteger) tuple.get(0);
    }

    @SuppressWarnings("unchecked")
    private List<Metric> getMetrics(InputStream inputStream, BigInteger length) throws IOException {
        byte[] bytes = new byte[length.intValue()];
        inputStream.read(bytes);
        String payload = new String(bytes, ISO_8859_1);

        PyString pyString = new PyString(payload);
        PyList pyList = (PyList) cPickle.loads(pyString);

        return Lists.transform(pyList, new PythonToJava());
    }

    private static class PythonToJava implements Function<Object, Metric> {
        @Override
        public Metric apply(Object pyObject) {
            PyTuple pyTuple = (PyTuple) pyObject;
            Metric metric = new Metric();
            metric.setName(pyTuple.get(0).toString());
            PyTuple data = ((PyTuple) pyTuple.get(1));
            metric.setTimestamp(new Date(((Number) data.get(0)).longValue() * 1000));
            metric.setValue(new BigDecimal(((Number) data.get(1)).doubleValue()));
            return metric;
        }
    }
}